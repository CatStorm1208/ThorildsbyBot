package de.catstorm.ThorildsbyBot;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

class Alcohol{
    public double prob;
}

class Classes{
    public double cannabis;
    public double cannabis_logo_only;
    public double cannabis_plant;
    public double cannabis_drug;
    public double recreational_drugs_not_cannabis;
    public double pills;
    public double paraphernalia;
    public double very_bloody;
    public double slightly_bloody;
    public double body_organ;
    public double serious_injury;
    public double superficial_injury;
    public double corpse;
    public double skull;
    public double unconscious;
    public double body_waste;
    public double other;
}

class CleavageCategories{
    public double very_revealing;
    public double revealing;
    public double none;
}

class Context{
    public double sea_lake_pool;
    public double outdoor_other;
    public double indoor_other;
}

class Face{
    public double x1;
    public double y1;
    public double x2;
    public double y2;
    public Features features;
}

class Features{
    public LeftEye left_eye;
    public RightEye right_eye;
    public NoseTip nose_tip;
    public LeftMouthCorner left_mouth_corner;
    public RightMouthCorner right_mouth_corner;
}

class Gore{
    public double prob;
    public Classes classes;
    public Type type;
}

class LeftEye{
    public double x;
    public double y;
}

class LeftMouthCorner{
    public double x;
    public double y;
}

class MaleChestCategories{
    public double very_revealing;
    public double revealing;
    public double slightly_revealing;
    public double none;
}

class Media{
    public String id;
    public String uri;
}

class Medical{
    public double prob;
    public Classes classes;
}

class NoseTip{
    public double x;
    public double y;
}

class Nudity{
    public double sexual_activity;
    public double sexual_display;
    public double erotica;
    public double very_suggestive;
    public double suggestive;
    public double mildly_suggestive;
    public SuggestiveClasses suggestive_classes;
    public double none;
    public Context context;
}

class Offensive{
    public double nazi;
    public double asian_swastika;
    public double confederate;
    public double supremacist;
    public double terrorist;
    public double middle_finger;
}

class Qr {
    public ArrayList<Object> personal;
    public ArrayList<Object> link;
    public ArrayList<Object> social;
    public ArrayList<Object> spam;
    public ArrayList<Object> profanity;
    public ArrayList<Object> blacklist;
}

class RecreationalDrug{
    public double prob;
    public Classes classes;
}

class Request{
    public String id;
    public double timestamp;
    public int operations;
}

class RightEye{
    public double x;
    public double y;
}

class RightMouthCorner{
    public double x;
    public double y;
}

public class SightEngineOut{
    public String status;
    public Request request;
    public Nudity nudity;
    public RecreationalDrug recreational_drug;
    public Medical medical;
    public Alcohol alcohol;
    public Offensive offensive;
    public Scam scam;
    public ArrayList<Face> faces;
    public Text text;
    public Gore gore;
    public Qr qr;
    public Type type;
    @SerializedName("self-harm")
    public SelfHarm selfHarm;
    public Media media;
}

class Scam{
    public double prob;
}

class SelfHarm{
    public double prob;
    public Type type;
}

class SuggestiveClasses{
    public double bikini;
    public double cleavage;
    public CleavageCategories cleavage_categories;
    public double lingerie;
    public double male_chest;
    public MaleChestCategories male_chest_categories;
    public double male_underwear;
    public double miniskirt;
    public double minishort;
    public double nudity_art;
    public double schematic;
    public double sextoy;
    public double suggestive_focus;
    public double suggestive_pose;
    public double swimwear_male;
    public double swimwear_one_piece;
    public double visibly_undressed;
    public double other;
}

class Text{
    public ArrayList<Object> profanity;
    public ArrayList<Object> personal;
    public ArrayList<Object> link;
    public ArrayList<Object> social;
    public ArrayList<Object> extremism;
    public ArrayList<Object> medical;
    public ArrayList<Object> drug;
    public ArrayList<Object> weapon;
    @SerializedName("content-trade")
    public ArrayList<Object> contentTrade;
    @SerializedName("content-transaction")
    public ArrayList<Object> moneyTransaction;
    public ArrayList<Object> spam;
    public ArrayList<Object> violence;
    @SerializedName("self-harm")
    public ArrayList<Object> selfHarm;
}

class Type{
    public double animated;
    public double fake;
    public double real;
    public double ai_generated;
}