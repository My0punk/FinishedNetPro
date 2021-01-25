package sdns.app.masterfile;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.NoSuchElementException;

import sdns.serialization.A;
import sdns.serialization.AAAA;
import sdns.serialization.CName;
import sdns.serialization.MX;
import sdns.serialization.NS;
import sdns.serialization.ResourceRecord;
import sdns.serialization.ValidationException;

/**
 * This is donahoo's mock masterfile
 */
public class MasterFileFactoryMock {

	public static MasterFile makeMasterFile() throws Exception {
		return new MasterFile() {
			@Override
			public void search(String question, List<ResourceRecord> answers, List<ResourceRecord> nameservers,
					List<ResourceRecord> additionals)
					throws NoSuchElementException, NullPointerException, ValidationException {
			    try {
                    answers.add(new A(question, 5, (Inet4Address) InetAddress.getByAddress(new byte[] {1,2,3,4})));
                    answers.add(new AAAA(question, 6, (Inet6Address) InetAddress.getByName("1::1")));
                    answers.add(new MX(question, 7, "mx.com.", 12));
                    
                    nameservers.add(new NS(question, 8, "ns.com."));
                    
                    additionals.add(new CName(question,  8, "canon.org."));
                } catch (UnknownHostException e) {
                } 
			}
		};
	}
}
