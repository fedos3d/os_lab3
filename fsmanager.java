import java.util.ArrayList;

public class fsmanager {
    ArrayList<FAT> fats = new ArrayList<>();
    fsmanager(){}
    void addFAT(FAT.FatType fatType, String name, int clustersize, long volsize) {
        FAT curFat = new FAT(fatType, name, clustersize, volsize);
        fats.add(curFat);
    }
}
