package com.alpdroid.huGen10.util;

final class isoTPdefines {


    /**************************************************************
     * internal used defines
     *************************************************************/
    static final int ISOTP_RET_OK = 0;
    static final int ISOTP_RET_ERROR = -1;
    static final int ISOTP_RET_INPROGRESS = -2;
    static final int ISOTP_RET_OVERFLOW = -3;
    static final int ISOTP_RET_WRONG_SN = -4;
    static final int ISOTP_RET_NO_DATA = -5;
    static final int ISOTP_RET_TIMEOUT = -6;
    static final int ISOTP_RET_LENGTH = -7;

    /*  invalid bs */
    static final int ISOTP_INVALID_BS = 0xFFFF;

    /* ISOTP sender status */
    public enum IsoTpSendStatusTypes {
        ISOTP_SEND_STATUS_IDLE,
        ISOTP_SEND_STATUS_INPROGRESS,
        ISOTP_SEND_STATUS_ERROR,
    }

    /* ISOTP receiver status */
    public enum IsoTpReceiveStatusTypes {
        ISOTP_RECEIVE_STATUS_IDLE,
        ISOTP_RECEIVE_STATUS_INPROGRESS,
        ISOTP_RECEIVE_STATUS_FULL,
    }

    /* can fram defination */
    // defined (ISOTP_BYTE_ORDER_LITTLE_ENDIAN)
}

    class IsoTpPciType {
        byte reserve_1=0; // 4 bits flag
        byte type=0; // 4 bits flag
        byte[] reserve_2 = new byte[7];
    }

    /*
     * single frame
     * +-------------------------+-----+
     * | byte #0                 | ... |
     * +-------------------------+-----+
     * | nibble #0   | nibble #1 | ... |
     * +-------------+-----------+ ... +
     * | PCIType = 0 | SF_DL     | ... |
     * +-------------+-----------+-----+
     */
     class IsoTpSingleFrame {
        byte SF_DL=0; // 4 bits flag
        byte type=0; // 4 bits flag
        byte[] data = new byte[7];
    }

    /*
     * first frame
     * +-------------------------+-----------------------+-----+
     * | byte #0                 | byte #1               | ... |
     * +-------------------------+-----------+-----------+-----+
     * | nibble #0   | nibble #1 | nibble #2 | nibble #3 | ... |
     * +-------------+-----------+-----------+-----------+-----+
     * | PCIType = 1 | FF_DL                             | ... |
     * +-------------+-----------+-----------------------+-----+
     */
     class IsoTpFirstFrame {
        byte FF_DL_high; // 4 bits
        byte type; // 4 bits
        byte FF_DL_low; //1 bit
        byte[] data = new byte[6];
    }

    /*
     * consecutive frame
     * +-------------------------+-----+
     * | byte #0                 | ... |
     * +-------------------------+-----+
     * | nibble #0   | nibble #1 | ... |
     * +-------------+-----------+ ... +
     * | PCIType = 0 | SN        | ... |
     * +-------------+-----------+-----+
     */
     class IsoTpConsecutiveFrame {
        byte SN=0; //4 bits
        byte type=0; //4 bits
        byte[] data = new byte[7];
    }

    /*
     * flow control frame
     * +-------------------------+-----------------------+-----------------------+-----+
     * | byte #0                 | byte #1               | byte #2               | ... |
     * +-------------------------+-----------+-----------+-----------+-----------+-----+
     * | nibble #0   | nibble #1 | nibble #2 | nibble #3 | nibble #4 | nibble #5 | ... |
     * +-------------+-----------+-----------+-----------+-----------+-----------+-----+
     * | PCIType = 1 | FS        | BS                    | STmin                 | ... |
     * +-------------+-----------+-----------------------+-----------------------+-----+
     */
     class IsoTpFlowControl {
        byte FS=0; //4 bits
        byte type=0; // 4 bits
        byte BS=0; // 1 bit
        byte STmin=0; // 1 bit
        byte[] reserve = new byte[5];
    }


     class IsoTpDataArray
     {
        byte[] ptr = new byte[8];
    }

     class IsoTpCanMessage
     {
        IsoTpPciType common=new IsoTpPciType();
        IsoTpSingleFrame single_frame=new IsoTpSingleFrame();
        IsoTpFirstFrame first_frame=new IsoTpFirstFrame();
        IsoTpConsecutiveFrame consecutive_frame=new IsoTpConsecutiveFrame();
        IsoTpFlowControl flow_control = new IsoTpFlowControl();
        IsoTpDataArray data_array = new IsoTpDataArray();

         public int sizeOf() {

             return (9+9+9+9+9+8); // 53 bits
         }

     }




