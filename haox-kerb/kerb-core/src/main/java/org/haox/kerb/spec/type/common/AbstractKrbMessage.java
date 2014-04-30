package org.haox.kerb.spec.type.common;

import org.haox.asn1.type.Asn1FieldInfo;
import org.haox.kerb.spec.KrbConstant;
import org.haox.kerb.spec.KrbException;
import org.haox.kerb.spec.type.KrbAppSequenceType;

public abstract class AbstractKrbMessage extends KrbAppSequenceType {
    protected static int PVNO = 0;
    protected static int MSG_TYPE = 1;

    private final int pvno = KrbConstant.KERBEROS_V5;

    public AbstractKrbMessage(KrbMessageType msgType, Asn1FieldInfo[] fieldInfos) throws KrbException {
        super(msgType.getValue(), fieldInfos);
        setPvno(pvno);
        setMsgType(msgType);
    }

    public int getPvno() {
        return pvno;
    }

    public KrbMessageType getMsgType() throws KrbException {
        Integer value = getFieldAsInteger(MSG_TYPE);
        return KrbMessageType.fromValue(value);
    }

    public void setMsgType(KrbMessageType msgType) throws KrbException {
        setFieldAsInt(MSG_TYPE, msgType.getValue());
    }

    protected void setPvno(int pvno) throws KrbException {
        setFieldAsInt(0, pvno);
    }
}
