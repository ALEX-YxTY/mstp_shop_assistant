package com.meishipintu.core.utils;

import java.io.File;
import java.text.DecimalFormat;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.app.MsptApplication;
import com.milai.utils.StringUtil;
import com.milai.download.DownloadMgr;

import android.os.Environment;


public class ConstUtil {

	public final static int REQUEST_LIMIT_COUNT = 15;
	
    public interface MSG_TYPE {
        public static final int RECOMMEND = 1;

        public static final int LIKE = 2;

        public static final int DINNER = 3;

    }
    
    public interface SN_STATUS {
        public static final int VALID = 1;
        public static final int USED = 2;
        public static final int EXPIRED = 3;

    }

    public interface LOGIN_TYPE {

        public static final byte TYPE_MSPT = 1;

        public static final byte TYPE_SINA = 2;
        
        public static final byte TYPE_QQ = 3;

        public static final byte TYPE_TENCENT = 4;

        public static final byte TYPE_RENREN = 5;

        public static final byte TYPE_DOUBAN = 6;

    }

    public interface MESSAGE_CODE {

        public static final int MSG_SSO_AUTH_DONE = 7;

        public static final int MSG_SSO_AUTH_FAIL = 8;

        public static final int MSG_SSO_SHARE_DONE = 9;

        public static final int MSG_SSO_SHARE_FAIL = 10;

    }

    public interface SHARE_TYPE {

        public static final byte TYPE_SINA = 4;

        public static final byte TYPE_TENCENT = 8;

        public static final byte TYPE_RENREN = 16;

        public static final byte TYPE_DOUBAN = 32;
    }

    public interface SERVER_REQUEST_RESULT {

        // public static final int RESPONSE_BAD_REQUEST = 0;

        public static final int RESPONSE_SERVER_QUERY_ERROR = 0;

        public static final int RESPONSE_COUNT_TOO_SMALL = -11;

        public static final int RESPONSE_FILE_TRANSFER_ERROR = -12;

        public static final int RESPONSE_MESSAGE_DELETED = -15;

        public static final int RESPONSE_NAME_LENGTH_ERROR = -16;

        public static final int RESPONSE_PASSWORD_LENGTH_ERROR = -17;

        public static final int RESPONSE_CONTENT_LENGTH_ERROR = -18;


        public static final int RESPONSE_ALREADY_PROCESSED = -20;

        public static final int RESPONSE_IN_PROCESSING = -21;

    }

    public interface REQUEST_CODE {

        public static final int PICK_FROM_CAMERA = 1;

        public static final int PICK_FROM_FILE = 2;

        public static final int CROP_IMAGE = 3;

        public static final int GET_SHOP_NAME = 4;

        public static final int AUTH_TENCENT = 5;

        public static final int GET_MSG_TEXT = 6;

        public static final int MSG_ADD_PIC = 7;

        public static final int SHARE_TO_SMS = 8;

        public static final int SHARE_TO_TENCENT = 9;

        public static final int SHARE_TO_SINA = 10;

        public static final int SHARE_TO_RENREN = 11;

        public static final int SHARE_TO_DOUBAN = 12;

        public static final int GET_AT_FANS_LIST = 13;
        
        public static final int SCRAMBLE_SEAT_NOW = 14;

        public static final byte TENCENT_OAUTH_2 = 15;

        public static final byte DOUBAN_OAUTH_2 = 16;
        
        public static final int AR_LOGIN_DONE = 17;
        
        public static final int AR_BOUND_TEL_DONE = 18;
        
        public static final int AR_LOGIN_LOGIN_BOUND = 19;
        
        public static final int SELECT_DISHES = 20;
        
        public static final int SELECT_TABLE = 21;
        
        public static final int TICKET_DRAFT = 22;
        
        public static final int PAY_TICKET = 23;
        
        public static final int PAY_TICKET_QR = 24;
        
    	public static final int CAPTURE_TICKET_NEWPAYMENT=25;
    	
    	public static final int PAY_TO_SCAN_REQUEST=30;//HCS
    	
    	public static final int PAY_REQUEST_VIP_INFO=31;
    }

    public interface INTENT_EXTRA_NAME {
        public static final String TENCENT_OAUTH = "tencent_oauth";
        public static final String DOUBAN_OAUTH = "douban_oauth";
        public static final String BUCKET_ID = "bucket_id";
        public static final String BUCKET_NAME = "bucket_name";
        public static final String IMAGE_IDS = "image_ids";
        public static final String TEMP_FILE_PATH = "temp_file_path";
        public static final String THUMB_URI = "thumb_uri";
        public static final String PIC_URI = "pic_uri";
        public static final String FANS_LIST = "fans";
        public static final String CITY_SLECTION_AUTOLOC = "auto_locate";
        public static final String SUCCESS_TYPE = "type";
        public static final String FOOD_CONTENT = "food_content";
        public static final String FOOD_IMG_URL = "food_img_url";
        public static final String YUE_FAN_CONTENT = "subject";
        public static final String YUE_FAN_MSG_ID = "msg_id";
        public static final String SHARE_PRESET_CONTENT = "preset_content";
        public static final String BACK_TO_MENU = "to_menu";
        public static final String SHARE_LON = "share_lon";
        public static final String SHARE_LAT = "share_lat";        
        public static final String PLAT_ID = "plat_id";        
        public static final String SHARE_TXT_PREFIX = "prefix";
        public static final String ADDRESS = "addr";
        public static final String ARRIVAL_TIME = "arrv_time";
        public static final String MONEY_AMOUNT = "money_amount";     
        public static final String TICKET_ID = "ticket_id";        
    }

    public interface PIC_AVAILABLE_TYPE {
        public static final byte NORMAL = 1;

        public static final byte USER_DELETE = 0;

        public static final byte SYSTEM_DELETE = -1;
    }
    






    public static final DecimalFormat decimalFormatDistance = new DecimalFormat("#0.00");


    public static final int INTENT_FROM_RECORD = 0;

    public static final int INTENT_FROM_PROFILE = 1;

    public static final int MSG_PIC_WIDTH = 180;

    public static final int MSG_PIC_HIGHT = 180;

    public static final int ROUND_AVATAR_WIDTH = 128;

    public static final int AVATAR_HIGHT = 128;

    public static String getAppFileDirPath() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return Environment.getExternalStorageDirectory() + File.separator + "mspt" + File.separator + "files";

        } else {
            return MsptApplication.getInstance().getFilesDir().getAbsolutePath();
        }
    }

    public static String getAppDir() {
        return Environment.getExternalStorageDirectory() + File.separator + "mspt";
    }

    public static final String getDownloadApkFilePath() {
        return Environment.getExternalStorageDirectory() + File.separator
                + MsptApplication.getInstance().getString(R.string.apk_name);
    }

    // TODO seem here we must use external storage directory, otherwise can not
    // get camera pic
    public static final String getUploadCacheDirPath() {
        // return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/"
        // + MyApplication.getInstance().getPackageName() + "/cache/upload";
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return Environment.getExternalStorageDirectory() + File.separator + "mspt" + File.separator + "upload";
        } else {
            return MsptApplication.getInstance().getCacheDir().getAbsolutePath() + File.separator + "upload";
        }
    }

    // public static String getTempFilePath() {
    // String state = Environment.getExternalStorageState();
    // if (Environment.MEDIA_MOUNTED.equals(state)) {
    // return Environment.getExternalStorageDirectory() + File.separator + "mspt" + File.separator + "disclose";
    // } else {
    // return MyApplication.getInstance().getCacheDir().getAbsolutePath() + File.separator + "disclose";
    // }
    // }

    public static String getTempFilePathAndCreateFile(long time) {
        // return ConstantsUtil.getUploadCacheDirPath() + File.separator + time + ".jpg";
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return Environment.getExternalStorageDirectory() + File.separator + "mspt" + File.separator + "upload"
                    + File.separator + time + ".jpg";
        } else {
            return MsptApplication.getInstance().getCacheDir().getAbsolutePath() + File.separator + "upload"
                    + File.separator + time + ".jpg";
        }
    }

    public static String getWebServerUrl() {
        // return MyApplication.getInstance().getString(R.string.web_server_url);
        return Cookies.getWebSvr();
    }


    public static String getFileServerUrl(String relativeUrl) {
        if (StringUtil.isNullOrEmpty(relativeUrl)) {
            return "";
        }
        String fileServerUrl = Cookies.getFileSvr();
        if (relativeUrl.startsWith(File.separator)) {
            return fileServerUrl + relativeUrl;
        } else {
            return fileServerUrl + File.separator + relativeUrl;
        }
    }

    public static String getTempFileNameOfStartPage() {
        return DownloadMgr.getDownloadCacheDirPath(MsptApplication.getInstance()) + File.separator + TEMP_DOWNLOAD_STARTPG_FILE_NAME;
    }

    public static String getFileNameOfStartPage() {
        return DownloadMgr.getDownloadCacheDirPath(MsptApplication.getInstance()) + File.separator + START_PAGE_FILE_NAME;
    }

    public static String getWeixinId() {
        return "wxb311259c60703100"; // formal signature
        // return "wx67d62c21b01db052"; //mydebug
    }

    public static String getSinaRedirectUrl() {
        return "http://www.meishipintu.com";
    }

    public static String getServiceMailbox() {
        return "support@kplink.cn";//hcs
    }

    public static String getServiceTel() {
        return "4008088017";
    }

    public static String getCompanyWebsite() {
        return "http://www.milaipay.com";//hcs
    }

    public static String getReportCateMailAddr() {
        return "info@meishipintu.com";
    }

    public static String getIMaxAppID() {
        return "0499350941";
    }

    public static String getIMaxAppKey() {
        return "7553ef2b3ee14c684f1b58e6e4c6c30d";
    }
    
    public static String getMsptDownloadAddress() {
        return "http://www.meishipintu.com/m/down.html";
    }

    public static final String VERSION_BEAN = "version_bean";

    public static final String USER_ID = "user_id";

    public static final String MSG_ID = "msg_id";

    public static final String SHOP_ID = "shop_id";

    public static final String USER_NAME = "user_name";

    public static final String MSG_PIC_ID = "msg_pic_id";
    
    public static final String DISH_TICKET_ID = "ticket_id";

    public static final String MSG = "msg";

    public static final byte TRUE = 1;

    public static final byte FALSE = 0;

    public static final String EDIT_TEXT_TITLE = "edit_text_title";

    public static final String TEXT = "text";

    public static final String PROVINCE = "province";

    public static final String CITY = "city";

    public static final String TEXT_SIZE_LIMIT = "text_size_limit";

    public static final short TEXT_SIZE_LIMIT_USER_NAME = 32;

    public static final short TEXT_SIZE_LIMIT_TEL = 11;

    public static final short TEXT_SIZE_LIMIT_ABOUT_ME = 150;

    public static final String LOCATION = "location";

    public static final String SHOP_NAME = "shop_name";

    public static final String FILE_NAME = "file_path";

    public static final String TAB_POSITION = "tab_position";

    public static final String MSG_OUT_PUT_PIC = "msgOutput.jpg";

    public static final String USER_AVATER_TEMP_PIC = "userAvaterTemp.jpg";

    public static final String AVATAR_TEMP_PIC = "avatarTemp.jpg";

    public static final String USER_PROFILE = "user_profile";

    public static final String TOTAL_PRICE = "total_price";
    
    public static final String TOTAL_FEE="total_fee";
    
    public static final String TOTAL_QUANTITY = "total_quantity";
    
    public static final String TRADE_NUM="trade_num";
    
    public static final String TEL = "tel";
    
    public static final String WAITER_NOTE = "waiter_note";
    
    public static final String USER_TEL = "user_tel";
    
    public static final String USER_NOTE = "user_note";  
    
    public static final String TABLE_ID = "table_id";
    
    public static final String TABLE_NAME = "table_name";
    
    public static final String PEOPLE_NUM="people_num";
    
    public static final String STATUS = "status";
        
    public static final String CREATE_TIME = "create_time";
    
    public static final String PAY_TIME = "pay_time";   
    
    public static final String FILE_URI = "file_uri";

    public static final String SMS_BODY = "sms_body";

    public static final String SHOP_LIST = "shopList";

    public static final String PLACE_INFO = "place_info";

    public static final float ROUND_PIX_SIZE = 6;

    public static final int NET_NEWS_LOAD_COUNT = 20;

    public static final String TEMP_DOWNLOAD_STARTPG_FILE_NAME = "tempstartpage";

    public static final String START_PAGE_FILE_NAME = "startpage";

    public static final String SHOW_BACK_BTN = "show_back_btn";

    public static final long START_PAGE_TIME_OUT = 2500;

    public static final long DYNAMIC_START_PAGE_TIMEOUT = 1000;

    public static final String SEX = "sex";

    public static final String COMMENT_COUNT = "comment_count";

    public static final int MAX_PIN_TU_NUM = 9;

    public static String NEWS_ID = "news_id";

    public static final String TXT_PATTERN = "pattern";

    public static final String TXT_INPUT_TYPE = "input_type";

    public static final String TXT_HINT = "text_hint";

}
