package sdns.app.masterfile;

public class MasterFileFactory {

    /**
     * Makes a masterFile
     * @return a new MasterFile instance
     * @throws Exception
     *      If anything goes wrong on making an instance
     */
    public static MasterFile makeMasterFile() throws Exception{
        return new MasterFileTCP();
    }
}
