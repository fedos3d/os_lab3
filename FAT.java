import java.io.FileOutputStream;
import java.io.IOException;

class BPB {
    FAT.FatType ftype;

    //all fats
    long volsz = 0;
    String volumeName;

    int bytespersec = 0;
    int amofsecpercluster = 0;
    long amofclusters = 0;
    long amofsectors = 0;
    int rsvdseccount = 0;
    int fats = 2;
    String BS_jmpBoot = "eb3c90"; // "eb3c90"; // BS_jmpBoot (3bytes)
    String BS_OEMName = "4d5357494e342e31"; // (8 bytes) // MSWIN4.1
    String BPB_BytsPerSec; /// BPB_BytsPerSec (2 bytes) ?
    String BPB_SecPerClus; //BPB_SecPerClus (1 byte)
    //hence, if we multiply 2 previous values we get our cluster size! // it should not be greater 32k in order to work fine
    String BPB_RsvdSecCnt; // for fat 12 and fat 16 it's always like that! // BPB_RsvdSecCnt (2 bytes)
    String BPB_NumFATs = "02";// its should always be 2 for any FAT// BPB_NumFATs (1 byte)
    String BPB_RoorEntCnt;//its 16, not sure if it's correct BPB_RootEntCnt (2 bytes)
    int BPB_RootEntCnount;
    String BPB_TotSec16; // in fat12 it's a sector count, guess we need it when we set our image size? Not sure what to write there // BPB_TotSec16
    String BPB_Media = "F8"; // 0xF8 is the standard value for “fixed” (non-removable) media...guess we use non removable media or removable? // BPB_Media
    String BPB_FATSz16; // Not sure if it's correct. This field is the FAT12/FAT16 16-bit count of sectors occupied by ONE FAT // BPB_FATSz16
    String BPB_SecPerTrk = "0000";// eometry (volume is broken down into tracks by multiple heads and cylinders) // BPB_SecPerTrk
    String BPB_NumHeads = "0000"; // Same as previous value (relevant only for geometry staff) // BPB_NumHeads
    String BPB_HiddenSec = "00000000"; //value is relevant if disk is partitioned
    String BPB_TotSec32;

    //hex vals
    long FATsz16;
    long FATsz32;
    long totsec16;
    long totsec32;

    //fat 12 and 16 specific
    String BS_DrvNum = "00"; // Int 0x13 drive number (e.g. 0x80).
    // This field supports MS-DOS bootstrap and is set to
    // the INT 0x13 drive number of the
    // media(0x00 for floppy disks, 0x80 for hard disks). // BS_DrvNum
    String BS_Reserved1 = "00"; // sholud always be 0 // BS_Reserved1
    String BS_BootSig = "29"; // should always be 29 // BS_BootSig
    String BS_VolID = "F4F09640"; // kinda set to 101120(10.11.2020) it's a volume serial number, usually created out of date and time // BS_VolID //TODO:Figure out how to convert time and date to id
    String BS_VolLab; // Volume label: lolkekVLF // BS_VolLab
    String BS_FilSysType; // FAT12 // BS_FilSysType

    //fat32 specific extended
    String BPB_FATSz32;
    String BPB_ExtFlags = "0000"; //2 bytes // dunno what to write there
    String BPB_FSVer = "0000";
    String BPB_RootClus = "00000002"; //usually 2
    String BPB_FSInfo = "0001";
    String BPB_BkBootSec = "0006";
    String BPB_Reserved = "000000000000000000000000";

    BPB(FAT.FatType fatType, int clustSize, long volsize, String volname) {
        
        //work with input
        this.BS_VolLab = convertStringToHex(volname);
        this.volsz = volsize;
        ftype = fatType;

        //for now i will hardcode 512 byte sector and 1 sector per cluster and size of 1 474 560
        this.amofsecpercluster = 1;
        this.bytespersec = 512;
        this.amofclusters = volsize / 512;
        this.amofsectors = amofclusters * amofsecpercluster;
        System.out.println(amofclusters);
        
        if (fatType == FAT.FatType.FAT12) {
            this.BPB_RsvdSecCnt = "0100";
            this.rsvdseccount = 1;
            this.BPB_RoorEntCnt = "0010"; // 16
            this.BPB_RootEntCnount = 16;
            this.BPB_TotSec16 = converter(Long.toHexString(amofsectors));
            totsec16 = amofsectors;
            totsec32 = 0;
            this.BPB_TotSec32 = "0000";
            this.BS_FilSysType = "4641543132202020";
            this.BPB_BytsPerSec = converter(Integer.toHexString(bytespersec));
        }
        else if (fatType == FAT.FatType.FAT16) {
            this.BPB_RsvdSecCnt = "0100";
            this.rsvdseccount = 1;
            this.BPB_RoorEntCnt = "0200";
            this.BPB_RootEntCnount = 512;
            this.BPB_TotSec16 = converter(Long.toHexString(amofsectors));
            this.BPB_TotSec32 = "0000";
            totsec16 = amofsectors;
            totsec32 = 0;
            this.BS_FilSysType = "4641543136202020";
        } else if (fatType == FAT.FatType.FAT32) {
            this.BPB_RsvdSecCnt = "2000";
            this.rsvdseccount = 32;
            this.BPB_RoorEntCnt = "0000";
            this.BPB_RootEntCnount = 0;
            this.BPB_TotSec16 = "0000";
            totsec16 = 0;
            totsec32 = amofsectors;
            this.BPB_FATSz16 = "0000";
            this.BPB_TotSec32 = converter(Long.toHexString(amofsectors));
            this.BS_FilSysType = "4641543332202020";
        }
        this.BPB_BytsPerSec = converter(Integer.toHexString(bytespersec));
        this.BPB_SecPerClus = converter(Integer.toHexString(amofsecpercluster));
        calcFatsz();

        //here we check our cluster amount
        int RootDirSectors = ((BPB_RootEntCnount * 32) + (bytespersec - 1)) / bytespersec;
        System.out.println("Root dir sectors: " + RootDirSectors);
        long FirstDataSector = 0;
        long FATSz = 0;
        if(BPB_FATSz16 != "0000") {
            FATSz = FATsz16;
        } else {
            FATSz = FATsz32; // I guess this works only for FAT32
        }
        FirstDataSector = rsvdseccount + (fats * FATSz) + RootDirSectors;
        long TotSec = 0;
        long DataSec = 0;
        if(FATsz16 != 0) {
            FATSz = FATsz16;
        }else {
            FATSz = FATsz32;
        }if(FATsz16 != 0) {
            TotSec = totsec16;
        }else {
            TotSec = totsec32;
        }
        DataSec = TotSec -(rsvdseccount + (fats * FATSz) + RootDirSectors);

        long CountofClusters = DataSec / amofsecpercluster;
        if(CountofClusters < 4085) {
            System.out.println("FAT12");
            System.out.println("Count of clusters: " + CountofClusters);
            /* Volume is FAT12 */
            //fatType = FatType.FAT12;
        } else if(CountofClusters < 65525) {
            System.out.println("FAT16");
            /* Volume is FAT16 */
            //fatType = FatType.FAT16;
        } else {
            System.out.println("FAT32");
            /* Volume is FAT32 */
          // fatType = FatType.FAT32;
        }//needs to round down

    }
    String converter(String kek) {
        if (kek.length() % 2 != 0) {
            kek = "0" + kek;
        }
        String newbie = "";
        for (int i = kek.length(); i > 1; i = i - 2) {
            newbie += kek.substring(i - 2, i);
        }
        return newbie;
    }
    void calcFatsz() { //цэ какая то дичь?
        int RootDirSectors = ((BPB_RootEntCnount * 32) + ( - 1)) / bytespersec;
        long TmpVal1 = (this.volsz - (rsvdseccount + RootDirSectors));
        long TmpVal2 = (256 * amofsecpercluster) + fats;
        if(ftype == FAT.FatType.FAT32) {
            TmpVal2 = TmpVal2 / 2;
        }
        long FATSz = (TmpVal1 + (TmpVal2 - 1)) / TmpVal2;
        if(ftype == FAT.FatType.FAT32) {
            BPB_FATSz16 = "0000";
            FATsz16 = 0;
            BPB_FATSz32 = converter(Long.toHexString(FATSz));
            FATsz32 = FATSz;
        } else {
            BPB_FATSz16 = converter(Long.toHexString(FATSz & 0xffff)); // че это за херня нифига не понимаю....
            //System.out.println("Fat sz 16: " + BPB_FATSz16);
            FATsz16 = FATSz & 0xffff;
            System.out.println("Fat sz 16: " + FATsz16);
            /* there is no BPB_FATSz32 in a FAT16 BPB */
        }
    }
    String convertStringToHex(String str) {

        StringBuffer hex = new StringBuffer();

        // loop chars one by one
        for (char temp : str.toCharArray()) {

            // convert char to int, for char `a` decimal 97
            int decimal = (int) temp;

            // convert int to hex, for decimal 97 hex 61
            hex.append(Integer.toHexString(decimal));
        }

        return hex.toString();

    }
    String getbytes() {
        String res = "";
        res += BS_jmpBoot;
        res += BS_OEMName;
        res += BPB_BytsPerSec;
        res += BPB_SecPerClus;
        res += BPB_RsvdSecCnt;
        res += BPB_NumFATs;
        res += BPB_RoorEntCnt;
        res += BPB_TotSec16;
        res += BPB_Media;
        res += BPB_FATSz16;
        res += BPB_SecPerTrk;
        res += BPB_NumHeads;
        res += BPB_HiddenSec;
        res += BPB_TotSec32;
        if (ftype == FAT.FatType.FAT12 || ftype == FAT.FatType.FAT16) {
            res += BS_DrvNum;
            res += BS_Reserved1;
            res += BS_BootSig;
            res += BS_VolID;
            res += BS_VolLab;
            res += BS_FilSysType;
        } else {
            res += BPB_FATSz32;
            res += BPB_ExtFlags;
            res += BPB_FSVer;
            res += BPB_RootClus;
            res += BPB_FSInfo;
            res += BPB_BkBootSec;
            res += BPB_Reserved;
            res += BS_DrvNum;
            res += BS_Reserved1;
            res += BS_BootSig;
            res += BS_VolID;
            res += BS_VolLab;
            res += BS_FilSysType;
        }
        System.out.println(bytespersec);
        if ((res.length() / 2) != bytespersec - 2) {
            while ((res.length() / 2) != bytespersec - 2) {
                res += "00";
            }
        }
        res += "55AA";
        return res;
    }
}

class FATTable {
    FATTable() {

    }
}

public class FAT {
    enum FatType {
        FAT12,
        FAT16,
        FAT32;
    }
    //User input info:
    FatType fatType;
    String volLabel;
    int clusterSize;

    //Fields of our fat:
    BPB bpb;

    FAT(FatType fatType, String name, int clustersize, long volsize){
        this.fatType = fatType;
        this.volLabel = name;
        this.clusterSize = clustersize;
        bpb = new BPB(fatType, clustersize, volsize, name);
        try (FileOutputStream stream = new FileOutputStream("C:\\Users\\Professional\\IdeaProjects\\os_lab3\\out\\production\\os_lab3\\images\\kek.img")) {
            stream.write(hexStringToByteArray(bpb.getbytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
