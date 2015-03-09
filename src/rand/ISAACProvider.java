package rand;

/**
 * @author Daniel Berlin
 */
public final class ISAACProvider extends java.security.Provider {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String NAME = "ISAACProvider";
	public static final double VERSION = 0.3;
	public static final String INFO = "Provider for the ISAAC PRNG";

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ISAACProvider() {
		super(NAME, VERSION, INFO);

		java.security.AccessController
				.doPrivileged(new java.security.PrivilegedAction() {
					public Object run() {
						put("SecureRandom.ISAACRandom", "rand.ISAACEngine");
						return (null);
					}
				});
	}
}