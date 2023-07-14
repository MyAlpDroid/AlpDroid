package com.alpdroid.huGen10.util;

/* This Class is rewriting in Java from Library provide by ISO-TP (ISO 15765-2) Support Library in C
https://github.com/lishen2/isotp-c
*/

import static com.alpdroid.huGen10.util.isoTPdefines.ISOTP_INVALID_BS;
import static com.alpdroid.huGen10.util.isoTPdefines.ISOTP_RET_ERROR;
import static com.alpdroid.huGen10.util.isoTPdefines.ISOTP_RET_INPROGRESS;
import static com.alpdroid.huGen10.util.isoTPdefines.ISOTP_RET_LENGTH;
import static com.alpdroid.huGen10.util.isoTPdefines.ISOTP_RET_NO_DATA;
import static com.alpdroid.huGen10.util.isoTPdefines.ISOTP_RET_OK;
import static com.alpdroid.huGen10.util.isoTPdefines.ISOTP_RET_OVERFLOW;
import static com.alpdroid.huGen10.util.isoTPdefines.ISOTP_RET_WRONG_SN;
import static com.alpdroid.huGen10.util.isoTPdefines.IsoTpReceiveStatusTypes;
import static com.alpdroid.huGen10.util.isoTPdefines.IsoTpSendStatusTypes;
import static java.lang.System.currentTimeMillis;

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
    /* Max number of messages the receiver can receive at one time, this value
     * is affectied by can driver queue length
     */

           static byte ISO_TP_DEFAULT_BLOCK_SIZE=   8;

            /* The STmin parameter value specifies the minimum time gap allowed between
             * the transmission of consecutive frame network protocol data units
             */
            static int ISO_TP_DEFAULT_ST_MIN    =   0;

            /* This parameter indicate how many FC N_PDU WTs can be transmitted by the
             * receiver in a row.
             */
            static int ISO_TP_MAX_WFT_NUMBER   =    1;

            /* Private: The default timeout to use when waiting for a response during a
             * multi-frame send or receive.
             */
           static int ISO_TP_DEFAULT_RESPONSE_TIMEOUT=100;

            /* Private: Determines if by default, padding is added to ISO-TP message frames.
             */
            static boolean ISO_TP_FRAME_PADDING=true;
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
    IsoTpSendStatusTypes send_status;

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
    IsoTpReceiveStatusTypes receive_status;


    /**************************************************************
     * protocol specific defines
     *************************************************************/

    /* Private: Protocol Control Information (PCI) types, for identifying each frame of an ISO-TP message.
     */

    //  IsoTpProtocolControlInformation
    final static byte ISOTP_PCI_TYPE_SINGLE = 0x0;
    final static byte ISOTP_PCI_TYPE_FIRST_FRAME = 0x1;
    final static byte TSOTP_PCI_TYPE_CONSECUTIVE_FRAME = 0x2;
    final static byte ISOTP_PCI_TYPE_FLOW_CONTROL_FRAME = 0x3;


    /* Private: Protocol Control Information (PCI) flow control identifiers.
     */

    //  public enum IsoTpFlowStatus
    final static byte PCI_FLOW_STATUS_CONTINUE = 0x0;
    final static byte PCI_FLOW_STATUS_WAIT = 0x1;
    final static byte PCI_FLOW_STATUS_OVERFLOW = 0x2;

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

    /* return logic true if 'a' is after 'b' */
    public boolean IsoTpTimeAfter(int a, int b) {
        return ((b - a) < 0);
    }


    int isotp_send_flow_control( byte flow_status, byte block_size, byte st_min_ms) {

        com.alpdroid.huGen10.util.IsoTpCanMessage message=new com.alpdroid.huGen10.util.IsoTpCanMessage();
        int ret;

        /* setup message  */
        message.flow_control.type = ISOTP_PCI_TYPE_FLOW_CONTROL_FRAME;
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


///////////////////////////////////////////////////////
///                 PUBLIC FUNCTIONS                ///
///////////////////////////////////////////////////////

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
        this.receive_status = IsoTpReceiveStatusTypes.ISOTP_RECEIVE_STATUS_IDLE;
        this.send_status = IsoTpSendStatusTypes.ISOTP_SEND_STATUS_IDLE;
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

        int ret;

        /* only polling when operation in progress */
        if ( IsoTpSendStatusTypes.ISOTP_SEND_STATUS_INPROGRESS == this.send_status) {

            /* continue send data */
            if (/* send data if bs_remain is invalid or bs_remain large than zero */
                    (ISOTP_INVALID_BS == this.send_bs_remain || this.send_bs_remain > 0) &&
                            /* and if st_min is zero or go beyond interval time */
                            (0 == this.send_st_min || (0 != this.send_st_min && IsoTpTimeAfter(isotp_user_get_ms(), this.send_timer_st)))) {

                ret = isotp_send_consecutive_frame();
                if (ISOTP_RET_OK == ret) {
                    if (ISOTP_INVALID_BS != this.send_bs_remain) {
                        this.send_bs_remain -= 1;
                    }
                    this.send_timer_bs = isotp_user_get_ms() + ISO_TP_DEFAULT_RESPONSE_TIMEOUT;
                    this.send_timer_st = isotp_user_get_ms() + this.send_st_min;

                    /* check if send finish */
                    if (this.send_offset >= this.send_size) {
                        this.send_status = IsoTpSendStatusTypes.ISOTP_SEND_STATUS_IDLE;
                    }
                } else {
                    this.send_status = IsoTpSendStatusTypes.ISOTP_SEND_STATUS_ERROR;
                }
            }

            /* check timeout */
            if (IsoTpTimeAfter(isotp_user_get_ms(), this.send_timer_bs)) {
                this.send_protocol_result = ISOTP_PROTOCOL_RESULT_TIMEOUT_BS;
                this.send_status = IsoTpSendStatusTypes.ISOTP_SEND_STATUS_ERROR;
            }
        }

        /* only polling when operation in progress */
        if (IsoTpReceiveStatusTypes.ISOTP_RECEIVE_STATUS_INPROGRESS == this.receive_status) {

            /* check timeout */
            if (IsoTpTimeAfter(isotp_user_get_ms(), this.receive_timer_cr)) {
                this.receive_protocol_result = ISOTP_PROTOCOL_RESULT_TIMEOUT_CR;
                this.receive_status = IsoTpReceiveStatusTypes.ISOTP_RECEIVE_STATUS_IDLE;
            }
        }

        return;
    }


    /**
     * @param data The data received via CAN.
     * @param len  The length of the data received.
     * @brief Handles incoming CAN messages.
     * Determines whether an incoming message is a valid ISO-TP frame or not and handles it accordingly.
     */
    void isotp_on_can_message(byte[] data, byte len) {
        IsoTpCanMessage message = new IsoTpCanMessage();
        int ret;

        if (len < 2 || len > 8) {
            return;
        }

   //     memcpy(message.data_array.ptr, data, len);
   //     memset(message.data_array.ptr + len, 0, message.data_array.ptr.length - len);

        switch (message.common.type) {
            case ISOTP_PCI_TYPE_SINGLE: {
                /* update protocol result */
                if (IsoTpReceiveStatusTypes.ISOTP_RECEIVE_STATUS_INPROGRESS == this.receive_status) {
                    this.receive_protocol_result = ISOTP_PROTOCOL_RESULT_UNEXP_PDU;
                } else {
                    this.receive_protocol_result = ISOTP_PROTOCOL_RESULT_OK;
                }

                /* handle message */
                ret = isotp_receive_single_frame(message, len);

                if (ISOTP_RET_OK == ret) {
                    /* change status */
                    this.receive_status = IsoTpReceiveStatusTypes.ISOTP_RECEIVE_STATUS_FULL;
                }
                break;
            }
            case ISOTP_PCI_TYPE_FIRST_FRAME: {
                /* update protocol result */
                if (IsoTpReceiveStatusTypes.ISOTP_RECEIVE_STATUS_INPROGRESS == this.receive_status) {
                    this.receive_protocol_result = ISOTP_PROTOCOL_RESULT_UNEXP_PDU;
                } else {
                    this.receive_protocol_result = ISOTP_PROTOCOL_RESULT_OK;
                }

                /* handle message */
                ret = isotp_receive_first_frame(message, len);

                /* if overflow happened */
                if (ISOTP_RET_OVERFLOW == ret) {
                    /* update protocol result */
                    this.receive_protocol_result = ISOTP_PROTOCOL_RESULT_BUFFER_OVFLW;
                    /* change status */
                    this.receive_status = IsoTpReceiveStatusTypes.ISOTP_RECEIVE_STATUS_IDLE;
                    /* send error message */
                    isotp_send_flow_control(PCI_FLOW_STATUS_OVERFLOW, (byte) 0, (byte) 0);
                    break;
                }

                /* if receive successful */
                if (ISOTP_RET_OK == ret) {
                    /* change status */
                    this.receive_status = IsoTpReceiveStatusTypes.ISOTP_RECEIVE_STATUS_INPROGRESS;
                    /* send fc frame */
                    this.receive_bs_count = ISO_TP_DEFAULT_BLOCK_SIZE;
                    isotp_send_flow_control(PCI_FLOW_STATUS_CONTINUE, this.receive_bs_count, (byte) ISO_TP_DEFAULT_ST_MIN);
                    /* refresh timer cs */
                    this.receive_timer_cr = isotp_user_get_ms() + ISO_TP_DEFAULT_RESPONSE_TIMEOUT;
                }

                break;
            }
            case TSOTP_PCI_TYPE_CONSECUTIVE_FRAME: {
                /* check if in receiving status */
                if (IsoTpReceiveStatusTypes.ISOTP_RECEIVE_STATUS_INPROGRESS != this.receive_status) {
                    this.receive_protocol_result = ISOTP_PROTOCOL_RESULT_UNEXP_PDU;
                    break;
                }

                /* handle message */
                ret = isotp_receive_consecutive_frame(message, len);

                /* if wrong sn */
                if (ISOTP_RET_WRONG_SN == ret) {
                    this.receive_protocol_result = ISOTP_PROTOCOL_RESULT_WRONG_SN;
                    this.receive_status = IsoTpReceiveStatusTypes.ISOTP_RECEIVE_STATUS_IDLE;
                    break;
                }

                /* if success */
                if (ISOTP_RET_OK == ret) {
                    /* refresh timer cs */
                    this.receive_timer_cr = isotp_user_get_ms() + ISO_TP_DEFAULT_RESPONSE_TIMEOUT;

                    /* receive finished */
                    if (this.receive_offset >= this.receive_size) {
                        this.receive_status = IsoTpReceiveStatusTypes.ISOTP_RECEIVE_STATUS_FULL;
                    } else {
                        /* send fc when bs reaches limit */
                        if (0 == --this.receive_bs_count) {
                            this.receive_bs_count = ISO_TP_DEFAULT_BLOCK_SIZE;
                            isotp_send_flow_control(PCI_FLOW_STATUS_CONTINUE, this.receive_bs_count, (byte) ISO_TP_DEFAULT_ST_MIN);
                        }
                    }
                }

                break;
            }
            case ISOTP_PCI_TYPE_FLOW_CONTROL_FRAME:
                /* handle fc frame only when sending in progress  */
                if (IsoTpSendStatusTypes.ISOTP_SEND_STATUS_INPROGRESS != this.send_status) {
                    break;
                }

                /* handle message */
                ret = isotp_receive_flow_control_frame(message, len);

                if (ISOTP_RET_OK == ret) {
                    /* refresh bs timer */
                    this.send_timer_bs = isotp_user_get_ms() + ISO_TP_DEFAULT_RESPONSE_TIMEOUT;

                    /* overflow */
                    if (PCI_FLOW_STATUS_OVERFLOW == message.flow_control.FS) {
                        this.send_protocol_result = ISOTP_PROTOCOL_RESULT_BUFFER_OVFLW;
                        this.send_status = IsoTpSendStatusTypes.ISOTP_SEND_STATUS_ERROR;
                    }

                    /* wait */
                    else if (PCI_FLOW_STATUS_WAIT == message.flow_control.FS) {
                        this.send_wtf_count += 1;
                        /* wait exceed allowed count */
                        if (this.send_wtf_count > ISO_TP_MAX_WFT_NUMBER) {
                            this.send_protocol_result = ISOTP_PROTOCOL_RESULT_WFT_OVRN;
                            this.send_status = IsoTpSendStatusTypes.ISOTP_SEND_STATUS_ERROR;
                        }
                    }

                    /* permit send */
                    else if (PCI_FLOW_STATUS_CONTINUE == message.flow_control.FS) {
                        if (0 == message.flow_control.BS) {
                            this.send_bs_remain = ISOTP_INVALID_BS;
                        } else {
                            this.send_bs_remain = message.flow_control.BS;
                        }
                        this.send_st_min = isotp_st_min_to_ms(message.flow_control.STmin);
                        this.send_wtf_count = 0;
                    }
                }
                break;
            default:
                break;
        };

        return;
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
        return isotp_send_with_id(this.send_arbitration_id, payload, size);
    }

    /**
     * @brief See @link isotp_send @endlink, with the exception that this function is used only for functional addressing.
     */
    int isotp_send_with_id(int id, byte[] payload, int size){
        int ret;

        if (this == null) {
            isotp_user_debug("Link is null!");
            return ISOTP_RET_ERROR;
        }

        if (size > this.send_buf_size) {
            isotp_user_debug("Message size too large. Increase ISO_TP_MAX_MESSAGE_SIZE to set a larger buffer\n");
        //    char message[128];
        //    sprintf(&message[0], "Attempted to send %d bytes; max size is %d!\n", size, this.send_buf_size);
            return ISOTP_RET_OVERFLOW;
        }

        if (IsoTpSendStatusTypes.ISOTP_SEND_STATUS_INPROGRESS == this.send_status) {
            isotp_user_debug("Abort previous message, transmission in progress.\n");
            return ISOTP_RET_INPROGRESS;
        }

        /* copy into local buffer */
        this.send_size = size;
        this.send_offset = 0;
     //   (void) memcpy(this.send_buffer, payload, size);

        if (this.send_size < 8) {
            /* send single frame */
            ret = isotp_send_single_frame(id);
        } else {
            /* send multi-frame */
            ret = isotp_send_first_frame(id);

            /* init multi-frame control flags */
            if (ISOTP_RET_OK == ret) {
                this.send_bs_remain = 0;
                this.send_st_min = 0;
                this.send_wtf_count = 0;
                this.send_timer_st = isotp_user_get_ms();
                this.send_timer_bs = isotp_user_get_ms() + ISO_TP_DEFAULT_RESPONSE_TIMEOUT;
                this.send_protocol_result = ISOTP_PROTOCOL_RESULT_OK;
                this.send_status = IsoTpSendStatusTypes.ISOTP_SEND_STATUS_INPROGRESS;
            }
        }

        return ret;
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
    int isotp_receive(byte[] payload, int payload_size, int out_size){
        int copylen;

        if (IsoTpReceiveStatusTypes.ISOTP_RECEIVE_STATUS_FULL != this.receive_status) {
            return ISOTP_RET_NO_DATA;
        }

        copylen = this.receive_size;
        if (copylen > payload_size) {
            copylen = payload_size;
        }

    //    memcpy(payload, this.receive_buffer, copylen);
    // out_size = reference to data ?? voir si ce n'est pas this...
        out_size = copylen;

        this.receive_status = IsoTpReceiveStatusTypes.ISOTP_RECEIVE_STATUS_IDLE;

        return ISOTP_RET_OK;
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
    {return (int) currentTimeMillis();}

}
