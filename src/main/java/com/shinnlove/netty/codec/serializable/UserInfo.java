/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.shinnlove.netty.codec.serializable;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * @author shinnlove.jinsheng
 * @version $Id: UserInfo.java, v 0.1 2018-06-29 下午1:53 shinnlove.jinsheng Exp $$
 */
public class UserInfo implements Serializable {

    /**
     * 默认的序列号
     */
    private static final long serialVersionUID = 1L;

    private String            userName;

    private int               userID;

    public UserInfo buildUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public UserInfo buildUserID(int userID) {
        this.userID = userID;
        return this;
    }

    /**
     * @return the userName
     */
    public final String getUserName() {
        return userName;
    }

    /**
     * @param userName
     *            the userName to set
     */
    public final void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the userID
     */
    public final int getUserID() {
        return userID;
    }

    /**
     * @param userID
     *            the userID to set
     */
    public final void setUserID(int userID) {
        this.userID = userID;
    }

    public byte[] codeC() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        byte[] value = this.userName.getBytes();
        buffer.putInt(value.length);
        buffer.put(value);
        buffer.putInt(this.userID);
        buffer.flip();
        value = null;
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return result;
    }

    public byte[] codeC(ByteBuffer buffer) {
        buffer.clear();
        byte[] value = this.userName.getBytes();
        buffer.putInt(value.length);
        buffer.put(value);
        buffer.putInt(this.userID);
        buffer.flip();
        value = null;
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return result;
    }

}