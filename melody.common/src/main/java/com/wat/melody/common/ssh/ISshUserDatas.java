package com.wat.melody.common.ssh;

import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ISshUserDatas {

	public String getLogin();

	/**
	 * Cannot be null or empty.
	 */
	public String setLogin(String sLogin);

	public String getPassword();

	/**
	 * Can be null, when the connection to remote system should be done with an
	 * empty password or with a keypair which have no passphrase.
	 */
	public String setPassword(String sPassword);

	public KeyPairRepository getKeyPairRepository();

	/**
	 * Can be null, when the connection to remote system should be done without
	 * any keypair.
	 */
	public KeyPairRepository setKeyPairRepository(
			KeyPairRepository keyPairRepository);

	public KeyPairName getKeyPairName();

	/**
	 * Can be null, when the connection to remote system should be done without
	 * any keypair.
	 */
	public KeyPairName setKeyPairName(KeyPairName keyPairName);

}