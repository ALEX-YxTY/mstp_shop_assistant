package com.meishipintu.core.utils;

import cn.weipass.pos.sdk.BizServiceInvoker;
import cn.weipass.pos.sdk.Printer;
import cn.weipass.pos.sdk.impl.WeiposImpl;

/**
 * Created by Administrator on 2016/7/5.
 */
public class WposServiceUtils {

    private static cn.weipass.pos.sdk.Printer wPosPrinter = null;
    private static BizServiceInvoker mBizServiceInvoker = null;

    private static Printer printer() {
        return WeiposImpl.as().openPrinter();
    }

    private static BizServiceInvoker bizServiceInvoker() {
        return WeiposImpl.as().openBizServiceInvoker();
    }

    public static Printer getPrinterInstance() {
        if (WeiposImpl.IsWeiposDevice()) {
            if (wPosPrinter == null) {
                wPosPrinter = printer();
            }
            return wPosPrinter;
        } else {
            return null;
        }
    }

    public static BizServiceInvoker getServiceInvokerInstance() {
        if (WeiposImpl.IsWeiposDevice()) {
            if (mBizServiceInvoker == null) {
                mBizServiceInvoker = bizServiceInvoker();
            }

            return mBizServiceInvoker;
        } else {
            return null;
        }
    }
}
