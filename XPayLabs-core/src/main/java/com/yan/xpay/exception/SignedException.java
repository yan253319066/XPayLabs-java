package com.yan.xpay.exception;

import org.dromara.common.core.exception.base.BaseException;

public class SignedException {
    public static class NullParam extends BaseException {
		private static final long serialVersionUID = 1L;

		public NullParam(String msg) {
            super(msg);
        }
    }

    public static class AppIdInvalid extends BaseException {
		private static final long serialVersionUID = 1L;

		public AppIdInvalid(String msg) {
            super(msg);
        }
    }

    public static class ReplayAttack extends BaseException {
		private static final long serialVersionUID = 1L;

		public ReplayAttack(String arg0, long arg1, String arg2) {
            super("appId: " + arg0 + ", timestamp: " + arg1 + ", nonce: " + arg2);
        }
    }

    public static class SignatureError extends BaseException {
		private static final long serialVersionUID = 1L;

		public SignatureError(String msg) {
            super(msg);
        }
    }

    public static class TimestampError extends BaseException {
		private static final long serialVersionUID = 1L;

		public TimestampError(String msg) {
            super(msg);
        }
    }
    
    public static class NoSuchAlgorithmException extends BaseException {
		private static final long serialVersionUID = 1L;

		public NoSuchAlgorithmException(String msg) {
            super(msg);
        }
    }
    
    public static class InvalidKeyException extends BaseException {
		private static final long serialVersionUID = 1L;

		public InvalidKeyException(String msg) {
            super(msg);
        }
		
    }
    
}
