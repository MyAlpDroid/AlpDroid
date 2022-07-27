package com.alpdroid.huGen10.util;

/* This Class is rewriting in Java from Library provide by ISO-TP (ISO 15765-2) Support Library in C
https://github.com/lishen2/isotp-c
*/

import static com.alpdroid.huGen10.util.IsoTpProtocolControlInformation.ISOTP_PCI_TYPE_FIRST_FRAME;
import static com.alpdroid.huGen10.util.IsoTpProtocolControlInformation.ISOTP_PCI_TYPE_SINGLE;
import static com.alpdroid.huGen10.util.IsoTpProtocolControlInformation.TSOTP_PCI_TYPE_CONSECUTIVE_FRAME;
import static com.alpdroid.huGen10.util.isoTPdefines.*;

import java.util.Arrays;

public class isoTPLink {

    /* Private: network layer result code.
     */

    static int ISOTP_PROTOCOL_RESULT_OK = 0;
    static int ISOTP_PROTOCOL_RESULT_TIMEOUT_A = -1;
    static int ISOTP_PROTOCOL_RESULT_TIMEOUT_BS = -2;
    static int ISOTP_PROTOCOL_RESULT_TIMEOUT_CR = -3;
    static int ISOTP_PROTOCOL_RESULT_WRONG_SN = -4;
    static int ISOTP_PROTOCOL_RESULT_INVALID_FS = -5;
    static int ISOTP_PROTOCOL_RESULT_UNEXP_PDU = -6;
    static int ISOTP_PROTOCOL_RESULT_WFT_OVRN = -7;
    static int ISOTP_PROTOCOL_RESULT_BUFFER_OVFLW = -8;
    static int ISOTP_PROTOCOL_RESULT_ERROR = -9;

    /**
     * @brief Struct containing the data for linking an application to a CAN instance.
     * The data stored in this struct is used internally and may be used by software programs
     * using this library.
     */
    /* sender paramters */
    int send_arbitration_id; /* used to reply consecutive frame */
    /* message buffer */
    byte[] send_buffer;
    int send_buf_size;
    int send_size;
    int send_offset;
    /* multi-frame flags */
    byte send_sn;
    int send_bs_remain; /* Remaining block size */
    byte send_st_min;    /* Separation Time between consecutive frames, unit millis */
    byte send_wtf_count; /* Maximum number of FC.Wait frame transmissions  */
    int send_timer_st;  /* Last time send consecutive frame */
    int send_timer_bs;  /* Time until reception of the next FlowControl N_PDU
                                                   start at sending FF, CF, receive FC
                                                   end at receive FC */
    int send_protocol_result;
    byte send_status;

    /* receiver paramters */
    int receive_arbitration_id;
    /* message buffer */
    byte[] receive_buffer;
    int receive_buf_size;
    int receive_size;
    int receive_offset;
    /* multi-frame control */
    byte receive_sn;
    byte receive_bs_count; /* Maximum number of FC.Wait frame transmissions  */
    int receive_timer_cr; /* Time until transmission of the next ConsecutiveFrame N_PDU
                                                     start at sending FC, receive CF
                                                     end at receive FC */
    int receive_protocol_result;
    byte receive_status;

///////////////////////////////////////////////////////
///                 STATIC FUNCTIONS                ///
///////////////////////////////////////////////////////

    /* st_min to microsecond */
    byte isotp_ms_to_st_min(byte ms) {
        byte st_min;

        st_min = ms;
        if (st_min > 0x7F) {
            st_min = 0x7F;
        }

        return st_min;
    }

    /* st_min to msec  */
     byte isotp_st_min_to_ms(byte st_min) {
        byte ms;

        if (st_min >= 0xF1 && st_min <= 0xF9) {
            ms = 1;
        } else if (st_min <= 0x7F) {
            ms = st_min;
        } else {
            ms = 0;
        }

        return ms;
    }



    int isotp_send_flow_control( byte flow_status, byte block_size, byte st_min_ms) {

        IsoTpCanMessage message=new IsoTpCanMessage();
        int ret;

        /* setup message  */
        message.flow_control.type = IsoTpProtocolControlInformation.ISOTP_PCI_TYPE_FLOW_CONTROL_FRAME;
        message.flow_control.FS = flow_status;
        message.flow_control.BS = block_size;
        message.flow_control.STmin = isotp_ms_to_st_min(st_min_ms);

        /* send message */
        /* by default PADDING to the end */

        ret = isotp_user_send_can(this.send_arbitration_id, message.data_array.ptr, message.sizeOf());

        return ret;
    }

    int isotp_send_single_frame(int id) {

        IsoTpCanMessage message = new IsoTpCanMessage();
        int ret;

        /* multi frame message length must greater than 7  */
        if ((this.send_size > 7)) throw new AssertionError();

        /* setup message  */
        message.single_frame.type = ISOTP_PCI_TYPE_SINGLE;
        message.single_frame.SF_DL = (byte) this.send_size;


        message.single_frame.data = Arrays.copyOf(send_buffer, this.send_size);

        /* send message with PADDING */
     //   (void) memset(message.single_frame.data + this.send_size, 0, sizeof(message.single_frame.data) - this.send_size);
        ret = isotp_user_send_can(id, message.data_array.ptr, message.sizeOf());

        return ret;
    }

    int isotp_send_first_frame(int id) {

        IsoTpCanMessage message = new IsoTpCanMessage();
        int ret;

        /* multi frame message length must greater than 7  */
        assert(this.send_size > 7);

        /* setup message  */
        message.first_frame.type = ISOTP_PCI_TYPE_FIRST_FRAME;
        message.first_frame.FF_DL_low = (byte) this.send_size;
        message.first_frame.FF_DL_high = (byte) (0x0F & (this.send_size >> 8));
     //   (void) memcpy(message.first_frame.data, this.send_buffer, sizeof(message.first_frame.data));

        /* send message */
        ret = isotp_user_send_can(id, message.data_array.ptr, message.sizeOf());
        if (ISOTP_RET_OK == ret) {
            this.send_offset += message.first_frame.data.length;
            this.send_sn = 1;
        }

        return ret;
    }

     int isotp_send_consecutive_frame() {

        IsoTpCanMessage message = new IsoTpCanMessage();
        int data_length;
        int ret;

        /* multi frame message length must greater than 7  */
        assert(this.send_size > 7);

        /* setup message  */
        message.consecutive_frame.type = TSOTP_PCI_TYPE_CONSECUTIVE_FRAME;
        message.consecutive_frame.SN = this.send_sn;
        data_length = this.send_size - this.send_offset;
        if (data_length >message.consecutive_frame.data.length) {
            data_length = message.consecutive_frame.data.length;
        }
    //    (void) memcpy(message.consecutive_frame.data, this.send_buffer + this.send_offset, data_length);

        /* send message */

   //     (void) memset(message.consecutive_frame.data + data_length, 0, sizeof(message.consecutive_frame.data) - data_length);
        ret = isotp_user_send_can(this.send_arbitration_id, message.data_array.ptr, message.sizeOf());

        if (ISOTP_RET_OK == ret) {
            this.send_offset += data_length;
            if (++(this.send_sn) > 0x0F) {
                this.send_sn = 0;
            }
        }

        return ret;
    }

     int isotp_receive_single_frame( IsoTpCanMessage message, byte len) {
        /* check data length */
        if ((0 == message.single_frame.SF_DL) || (message.single_frame.SF_DL > (len - 1))) {
            isotp_user_debug("Single-frame length too small.");
            return ISOTP_RET_LENGTH;
        }

        /* copying data */
  //      (void) memcpy(this.receive_buffer, message.single_frame.data, message.single_frame.SF_DL);
        this.receive_size = message.single_frame.SF_DL;

        return ISOTP_RET_OK;
    }

     int isotp_receive_first_frame( IsoTpCanMessage message, byte len) {
        int payload_length;

        if (8 != len) {
            isotp_user_debug("First frame should be 8 bytes in length.");
            return ISOTP_RET_LENGTH;
        }

        /* check data length */
        payload_length = message.first_frame.FF_DL_high;
        payload_length = (payload_length << 8) + message.first_frame.FF_DL_low;

        /* should not use multiple frame transmition */
        if (payload_length <= 7) {
            isotp_user_debug("Should not use multiple frame transmission.");
            return ISOTP_RET_LENGTH;
        }

        if (payload_length > this.receive_buf_size) {
            isotp_user_debug("Multi-frame response too large for receiving buffer.");
            return ISOTP_RET_OVERFLOW;
        }

        /* copying data */
//        (void) memcpy(this.receive_buffer, message.first_frame.data, sizeof(message.first_frame.data));
        this.receive_size = payload_length;
        this.receive_offset = message.first_frame.data.length;
        this.receive_sn = 1;

        return ISOTP_RET_OK;
    }

     int isotp_receive_consecutive_frame( IsoTpCanMessage message, byte len) {
        int remaining_bytes;

        /* check sn */
        if (this.receive_sn != message.consecutive_frame.SN) {
            return ISOTP_RET_WRONG_SN;
        }

        /* check data length */
        remaining_bytes = this.receive_size - this.receive_offset;
        if (remaining_bytes > message.consecutive_frame.data.length) {
            remaining_bytes = message.consecutive_frame.data.length;
        }
        if (remaining_bytes > len - 1) {
            isotp_user_debug("Consecutive frame too short.");
            return ISOTP_RET_LENGTH;
        }

        /* copying data */
     //   (void) memcpy(this.receive_buffer + this.receive_offset, message.consecutive_frame.data, remaining_bytes);

        this.receive_offset += remaining_bytes;
        if (++(this.receive_sn) > 0x0F) {
            this.receive_sn = 0;
        }

        return ISOTP_RET_OK;
    }

     int isotp_receive_flow_control_frame( IsoTpCanMessage message, byte len) {
        /* check message length */
        if (len < 3) {
            isotp_user_debug("Flow control frame too short.");
            return ISOTP_RET_LENGTH;
        }

        return ISOTP_RET_OK;
    }


    /**
     * @param sendid      The ID used to send data to other CAN nodes.
     * @param sendbuf     A pointer to an area in memory which can be used as a buffer for data to be sent.
     * @param sendbufsize The size of the buffer area.
     * @param recvbuf     A pointer to an area in memory which can be used as a buffer for data to be received.
     * @param recvbufsize The size of the buffer area.
     * @brief Initialises the ISO-TP library.
     */
    void isotp_init_link(int sendid,
                         byte[] sendbuf, int sendbufsize,
                         byte[] recvbuf, int recvbufsize) {
        this.receive_status = 0; // ISOTP_RECEIVE_STATUS_IDLE;
        this.send_status = 0; // ISOTP_SEND_STATUS_IDLE;
        this.send_arbitration_id = sendid;
        this.send_buffer = sendbuf;
        this.send_buf_size = sendbufsize;
        this.receive_buffer = recvbuf;
        this.receive_buf_size = recvbufsize;
    }

    /**
     * @brief Polling function; call this function periodically to handle timeouts, send consecutive frames, etc.
     */

    void isotp_poll() {
    }

    /**
     * @param data The data received via CAN.
     * @param len  The length of the data received.
     * @brief Handles incoming CAN messages.
     * Determines whether an incoming message is a valid ISO-TP frame or not and handles it accordingly.
     */
    void isotp_on_can_message(byte[] data, byte len) {
    }

    /**
     * @param payload The payload to be sent. (Up to 4095 bytes).
     * @param size    The size of the payload to be sent.
     * @return Possible return values:
     * - @code ISOTP_RET_OVERFLOW @endcode
     * - @code ISOTP_RET_INPROGRESS @endcode
     * - @code ISOTP_RET_OK @endcode
     * - The return value of the user shim function isotp_user_send_can().
     * @brief Sends ISO-TP frames via CAN, using the ID set in the initialising function.
     * <p>
     * Single-frame messages will be sent immediately when calling this function.
     * Multi-frame messages will be sent consecutively when calling isotp_poll.
     */
    int isotp_send(byte[] payload, int size) {
        return 0;
    }

    /**
     * @brief See @link isotp_send @endlink, with the exception that this function is used only for functional addressing.
     */
    int isotp_send_with_id(int id, byte[] payload, int size) {
        return 0;
    }

    /**
     * @param payload      A pointer to an area in memory where the raw data is copied from.
     * @param payload_size The size of the received (raw) CAN data.
     * @param out_size     A reference to a variable which will contain the size of the actual (parsed) data.
     * @return Possible return values:
     * - @link ISOTP_RET_OK @endlink
     * - @link ISOTP_RET_NO_DATA @endlink
     * @brief Receives and parses the received data and copies the parsed data in to the internal buffer.
     */
    int isotp_receive(byte[] payload, int payload_size, int out_size) {
        return 0;
    }



    /* user implemented, print debug message */
    void isotp_user_debug(String message)
    {}

    /* user implemented, send can message. should return ISOTP_RET_OK when success.
     */
    int  isotp_user_send_can(int arbitration_id,
                         byte[] data, int size)
    {
        return ISOTP_RET_OK;
    }

    /* user implemented, get millisecond */
    int isotp_user_get_ms()
    {return 0;}

}
