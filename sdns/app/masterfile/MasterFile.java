package sdns.app.masterfile;

import java.util.List;
import java.util.NoSuchElementException;

import sdns.serialization.ResourceRecord;
import sdns.serialization.ValidationException;

/**
 * SDNS Response RRs source
 * 
 * @version 1.0
 */
public interface MasterFile {
	/**
	 * Populate answer, name server, and additional list RRs.
	 * 
	 * @param question query for SDNS query
	 * @param answers RR list (allocated) to add answer RRs to
	 * @param nameservers RR list (allocated) to add name server RRs to
	 * @param additionals RR list (allocated) to add additional RRs to
	 * 
	 * @throws NoSuchElementException if no such domain name
	 * @throws NullPointerException if any parameters are null
	 * @throws ValidationException if question is invalid or anything else goes wrong while
	 * trying to resolve question
	 */
	void search(final String question, final List<ResourceRecord> answers, final List<ResourceRecord> nameservers,
			final List<ResourceRecord> additionals) throws NoSuchElementException, NullPointerException, ValidationException;
}
