package com.zx.appupgrade;

/**
 * Created by 周旭 on 2016/11/23.
 * <p>
 * "访问新版本的APP的Json数据"
 */

public class Version {

    /**
     * status : 0
     * error : ok
     * data : {"version":2,"vsersion_url":"http://oh0vbg8a6.bkt.clouddn.com/app-debug.apk"}
     */

    private int status;
    private String error;
    /**
     * version : 2
     * vsersion_url : http://oh0vbg8a6.bkt.clouddn.com/app-debug.apk
     */

    private DataBean data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private int version; //服务器的版本号
        private String vsersion_url; //服务器对应版本号的apk文件的Url地址

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public String getVsersion_url() {
            return vsersion_url;
        }

        public void setVsersion_url(String vsersion_url) {
            this.vsersion_url = vsersion_url;
        }
    }
}
