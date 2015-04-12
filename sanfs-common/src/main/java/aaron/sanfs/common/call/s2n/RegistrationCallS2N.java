package aaron.sanfs.common.call.s2n;

import aaron.sanfs.common.call.Call;

/**
 * Registration call, sent from <tt>StorageServer</tt> to <tt>NameServer</tt>.
 * <p/>
 * <tt>StorageServer</tt> should attach his own address to this call.
 *
 * @author lishunyang
 */
public class RegistrationCallS2N extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = 1475266407427118687L;

    /**
     * Address of this <tt>StorageServer</tt>
     */
    private String address;

    /**
     * Construction method.
     *
     * @param address
     */
    public RegistrationCallS2N(String address) {
        super(Call.Type.REGISTRATION_S2N);
        this.address = address;
    }

    /**
     * Get address of this <tt>StorageServer</tt>
     *
     * @return
     */
    public String getAddress() {
        return address;
    }
}

