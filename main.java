import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Scanner;

public class main {

    enum FatType {
        FAT12,
        FAT16,
        FAT32;
    }

    public static void main(String args[]) {
        //Here we dialouge our user:
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose which fs type do you want? 1 - FAT, 2 - EXT2");
        int fsOption = scanner.nextInt();
        if (fsOption != 1 && fsOption != 2) {
            System.out.println("Oh, you think you are smart? Now check this out:");
            System.exit(-1);
        }
        if (fsOption == 1) {
            System.out.println("Please specify which type of FAT do you want? 1 - FAT12, 2 - FAT16, 3- FAT32");
            int fatoption = scanner.nextInt();
            if (fatoption != 1 && fatoption != 2 && fatoption != 3) {
                System.out.println("Oh, you think you are smart? Now check this out:");
                System.exit(-1);
            }
            if (fatoption == 1 || fatoption == 2) {
                System.out.println("Please choose a volume label(It must not more than 11 chars):");
                String volName = scanner.next();
                if (volName.length() > 11) {
                    System.out.println("Oh, you think you are smart? Now check this out:");
                    System.exit(-1);
                } else if (volName.length() != 11) {
                    while (volName.length() != 11) {
                        volName += " ";
                    }
                }
                System.out.println("Please choose size for your volume: (I want to set size in 1 - bytes, 2 - KB, 3 - MB, 4 - GB, 5 - sectors");
                int sizeOption = scanner.nextInt();
                if (sizeOption > 5 || sizeOption < 1) {
                    System.out.println("Oh, you think you are smart? Now check this out:");
                    System.exit(-1);
                }
                System.out.println("Please set size: ");
                long volsize = scanner.nextInt();
                System.out.println("Please choose cluster size: 0 - for 512 bytes, 1 - 1KB, 2 - 2KB, 3 - 4KB, 4 - 8KB, 5 - 16KB, 6 - 32KB");
                int clusterSize = scanner.nextInt();
                if (clusterSize < 0 || clusterSize > 7) {
                    System.out.println("Oh, you think you are smart? Now check this out:");
                    System.exit(-1);
                }
                if (clusterSize == 0) {
                    clusterSize = 512;
                }
                if (sizeOption == 2) {
                    volsize = volsize * 1024;
                }
                if (sizeOption == 3) {
                    volsize = volsize * 1024 * 1024;
                }
                if (sizeOption == 4) {
                    volsize = volsize * 1024 * 1024 * 1024;
                }
                if (sizeOption == 5) { //not now

                }
                fsmanager fsmanager = new fsmanager();
                fsmanager.addFAT(FAT.FatType.FAT12, volName, clusterSize, volsize);
            }
            ;
        }
        ;
        while (true) {
            System.out.println("Here's a list of what you can do:");
            System.out.println("1 - list your fs images");
            System.out.println("0 - exit application");
            System.out.println("Your command: ");
            int command = scanner.nextInt();
            if (command != 0 && command != 1) {
                continue;
            }
            if (command == 1) {
                System.out.println("lul");
            } else if (command == 0) {
                System.exit(0);
            }
        }


        //let's start with fat12

        //bpb
        /*
        String filepath = "C:\\Users\\Professional\\IdeaProjects\\os_lab3\\out\\production\\os_lab3\\images\\fat121.img";
        String bytes = "eb3c90"; // BS_jmpBoot (3bytes)
        bytes += "46415431324c5646"; // BS_OEMName (8 bytes) // "FAT12LVF"
        int selecCustomClusterSize;
        bytes += "0002"; //че это значит? хз, видимо 2000 в нормальном виде, значит 512// BPB_BytsPerSec (2 bytes) ?
        int BPB_BytsPerSec = 0x0200;
        bytes += "01"; // BPB_SecPerClus (1 byte)
        int BPB_SecPerClus = 1;
        //hence, if we multiply 2 previous values we get our cluster size! // it should not be greater 32k in order to work fine
        bytes += "1000"; // for fat 12 and fat 16 it's always like that! // BPB_RsvdSecCnt (2 bytes)
        int BPB_ResvdSecCnt = 1;
        bytes += "02"; // its should always be 2 // BPB_NumFATs (1 byte)
        int BPB_NumFATs = 2;
        bytes += "0010"; //its 16, not sure if it's correct BPB_RootEntCnt (2 bytes)
        int BPB_RootEntCnt = 16;
        bytes += "400b"; // in fat12 it's a sector count, guess we need it when we set our image size? Not sure what to write there // BPB_TotSec16
        int BPB_TotSec16 = 2880;
        bytes += "F8"; // 0xF8 is the standard value for “fixed” (non-removable) media...guess we use non removable media or removable? // BPB_Media
        bytes += "0900"; // Not sure if it's correct. This field is the FAT12/FAT16 16-bit count of sectors occupied by ONE FAT // BPB_FATSz16
        int BPB_FATSz16 = 9;
        bytes += "0000"; //This field is only relevant for media that have a g
        // eometry (volume is broken down into tracks by multiple heads and cylinders) // BPB_SecPerTrk
        bytes += "0000"; // Same as previous value (relevant only for geometry staff) // BPB_NumHeads
        bytes += "00000000"; // Same as previous value // BPB_HiddSec
        bytes += "00000000"; //can be zero if BPB_TotSec16 is non zero // BPB_TotSec32
        int BPB_TotSec32 = 0;
        //bpb finished

        //bs for fat12 starting
        bytes += "00"; // Int 0x13 drive number (e.g. 0x80).
        // This field supports MS-DOS bootstrap and is set to
        // the INT 0x13 drive number of the
        // media(0x00 for floppy disks, 0x80 for hard disks). // BS_DrvNum
        bytes += "00"; // sholud always be 0 // BS_Reserved1
        bytes += "29"; // should always be 29 // BS_BootSig
        bytes += "00965210"; // kinda set to 101120(10.11.2020) it's a volume serial number, usually created out of date and time // BS_VolID
        bytes += "6c6f6c6b656b564c462020"; // Volume label: lolkekVLF // BS_VolLab
        bytes += "4641543132202020"; // FAT12 // BS_FilSysType
        //bs for fat12 ended

        int RootDirSectors = ((BPB_RootEntCnt * 32) + (BPB_BytsPerSec - 1)) / BPB_BytsPerSec;
        int FirstDataSector = 0;
        int FATSz = 0;
        if(BPB_FATSz16 != 0) {
            FATSz = BPB_FATSz16;
        } else {
            //FATSz = BPB_FATSz32; // I guess this works only for FAT32
        }
        FirstDataSector = BPB_ResvdSecCnt + (BPB_NumFATs * FATSz) + RootDirSectors;
        int TotSec = 0;
        int DataSec = 0;
        if(BPB_FATSz16 != 0) {
            FATSz = BPB_FATSz16;
        }else {
            //FATSz = BPB_FATSz32;
        }if(BPB_TotSec16 != 0) {
            TotSec = BPB_TotSec16;
        }else {
            TotSec = BPB_TotSec32;
        }
        DataSec = TotSec -(BPB_ResvdSecCnt + (BPB_NumFATs * FATSz) + RootDirSectors);

        int CountofClusters = DataSec / BPB_SecPerClus; //needs to round down

        //here we which fat type did we get
        FatType fatType = FatType.FAT32;
        if(CountofClusters < 4085) {
            System.out.println("FAT12");
            System.out.println("Count of clusters: " + CountofClusters);
            /* Volume is FAT12 */
        /*
            fatType = FatType.FAT12;
        } else if(CountofClusters < 65525) {
            System.out.println("FAT16");
            /* Volume is FAT16 */
        /*
            fatType = FatType.FAT16;
        } else {
            System.out.println("FAT32");
            /* Volume is FAT32 */
          /*  fatType = FatType.FAT32;
        }

        //code for fat12 // what kind of code?
        int N = 15; //it's a number of cluster...vrode by
        int FATOffset = 0;
        int ThisFATSecNum = 0;
        int ThisFATEntOffset = 0;
        if (fatType == FatType.FAT12) {
            FATOffset = N + (N / 2);
        }
        /* Multiply by 1.5 without using floating point, the divide by 2 rounds DOWN */
        /*ThisFATSecNum = BPB_ResvdSecCnt + (FATOffset / BPB_BytsPerSec);
        ThisFATEntOffset = FATOffset % BPB_BytsPerSec;
        if(ThisFATEntOffset == (BPB_BytsPerSec - 1)) {
            /* This cluster access spans a sector boundary in the FAT */
        /* There are a number of strategies to handling this. The */
        /* easiest is to always load FAT sectors into memory */
        /* in pairs if the volume is FAT12 (if you want to load */
        /* FAT sector N, you also load FAT sector N+1 immediately */
        /* following it in memory unless sector N is the last FAT */
        /* sector). It is assumed that this is the strategy used here */
        /* which makes this if test for a sector boundary span */
        /* unnecessary. */


        /*We now access the FAT entry as a WORD just as we do for FAT16, but if the cluster number is
        EVEN, we only want the low 12-bits of the 16-bits we fetch; and if the cluster number is ODD, we
        only want the high 12-bits of the 16-bits we fetch.
         */
        /*
        FAT12ClusEntryVal = *((WORD *) &SecBuff[ThisFATEntOffset]); //16 bit // didn't get this code line
        if(N & 0x0001) {
            FAT12ClusEntryVal = FAT12ClusEntryVal >> 4; /* Cluster number is ODD */
        /*} else {
            FAT12ClusEntryVal = FAT12ClusEntryVal & 0x0FFF; /* Cluster number is EVEN */
    //}


    //here we can write to cluster
    //if(N & 0x0001) {
    //    FAT12ClusEntryVal = FAT12ClusEntryVal << 4; /* Cluster number is ODD */
    //    *((WORD *) &SecBuff[ThisFATEntOffset]) = (*((WORD *) &SecBuff[ThisFATEntOffset])) & 0x000F;
    //} else {
    //    FAT12ClusEntryVal = FAT12ClusEntryVal & 0x0FFF; /* Cluster number is EVEN */
    //        *((WORD *) &SecBuff[ThisFATEntOffset]) = (*((WORD *) &SecBuff[ThisFATEntOffset])) & 0xF000;
    // }
    //    *((WORD *) &SecBuff[ThisFATEntOffset]) = (*((WORD *) &SecBuff[ThisFATEntOffset])) | FAT12ClusEntryVal;

    //root dir frist sec
        /*int FirstRootDirSecNum = BPB_ResvdSecCnt + (BPB_NumFATs * BPB_FATSz16);

        //extended BPB null bytes (for FAT12)
        String extended_boot_code = "";
        for (int i = 0; i < 28; i++) {
            extended_boot_code += "00";
        }
        bytes += extended_boot_code;
        //boot code which we now set to empty
        String bootcode = "";
        for (int i = 0; i < 420; i++) {
            bootcode += "00";
        }
        bytes += bootcode;
        bytes += "55AA"; //now set showed end of our cluster

        for (int m = 0; m < 2; m++) { //here we set our fat tables, we have 2 copies for redundency, value was assigned somewhere above
            //first 2 reserved sectors
            bytes += "F0FFFF";
            String resvsectors = "";
            for (int i = 0; i < 509; i++) {
                resvsectors += "00";
            }
            bytes += resvsectors;
            //bytes += "0FFF";
            resvsectors = "";
            for (int i = 0; i < 512; i++) {
                resvsectors += "00";
            }
            bytes += resvsectors;

            //fat data blocks
            //add 6 data blocks data block
            for (int k = 0; k < 6; k++) {
                String datablock = "";
                for (int i = 0; i < 512; i++) {
                    datablock += "00";
                }
                bytes += datablock;
            }
            //let's add last block
            String lastblock = "0FFF";
            for (int i = 0; i < 510; i++) {
                lastblock += "00";
            }
            bytes += lastblock;
        }

        //here i started adding root dir
        bytes += "4b454b4e4f5f5f5f"; //kinda added name
        bytes += "535953"; // file ext, dunno if it's correct
        bytes += "21"; // dunno, somekinda of file attr
        bytes += "00000000000000000000"; //dunno 10 empty bytes?
        bytes += "2463"; // last mod time
        bytes += "A430"; //last mod date
        bytes += "0200"; //first data cluster address
        bytes += "9C1E0000"; //file size
        byte[] bytesForOurFS = hexStringToByteArray(bytes);
        try (FileOutputStream stream = new FileOutputStream(filepath)) {
            stream.write(bytesForOurFS);
            for (int i = 0; i < 512 - 32; i++) {
                stream.write(hexStringToByteArray("00"));
            }
            for (int i = 0; i < CountofClusters - 20; i++) {
                for (int j = 0; j < 512; j++) {
                    stream.write(hexStringToByteArray("00"));
                    //writeToFile("00", filepath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*System.out.println(FirstRootDirSecNum);
        //fullfilled 32 bytes
        //fill root dir:
        bytes = "";
        for (int i = 0; i < 512 - 32; i++) {
            bytes += "00";
        }
        //writeToFile(bytes, filepath);
        //filled 20 clusters
        for (int i = 0; i < CountofClusters - 20; i++) {
            for (int j = 0; j < 512; j++) {
                //writeToFile("00", filepath);
            }
        }


         *///here we write our hex to a .img file
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
    public static void writeToFile(String bytes, String filepath) {
        byte[] bytesForOurFS = hexStringToByteArray(bytes);
        try (FileOutputStream stream = new FileOutputStream(filepath)) {
            stream.write(bytesForOurFS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
