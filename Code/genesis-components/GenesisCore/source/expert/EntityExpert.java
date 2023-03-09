package expert;

import java.util.*;

import utils.Mark;
import connections.*;
import connections.signals.BetterSignal;
import constants.Switch;
import frames.entities.Entity;
import frames.entities.Sequence;

/*
 * Created on Jan 28, 2014
 * @author phw
 */

public class EntityExpert extends AbstractWiredBox {

	public final static String STORY = "Story port";

	private TreeSet<String> types = new TreeSet<>();

	private HashSet<String> focii = new HashSet<>();

	public EntityExpert() {
		super("Entity expert");
		addBasicEnglishWords();
		Connections.getPorts(this).addSignalProcessor(STORY, "processSignal");
	}

	public void addBasicEnglishWords() {
		focii.add("come");
		focii.add("get");
		focii.add("give");
		focii.add("go");
		focii.add("keep");
		focii.add("let");
		focii.add("make");
		focii.add("put");
		focii.add("seem");
		focii.add("take");
		focii.add("be");
		focii.add("do");
		focii.add("have");
		focii.add("say");
		focii.add("see");
		focii.add("send");
		focii.add("may");
		focii.add("will");
		focii.add("about");
		focii.add("across");
		focii.add("after");
		focii.add("against");
		focii.add("among");
		focii.add("at");
		focii.add("before");
		focii.add("between");
		focii.add("by");
		focii.add("down");
		focii.add("from");
		focii.add("in");
		focii.add("off");
		focii.add("on");
		focii.add("over");
		focii.add("through");
		focii.add("to");
		focii.add("under");
		focii.add("up");
		focii.add("with");
		focii.add("as");
		focii.add("for");
		focii.add("of");
		focii.add("till");
		focii.add("than");
		focii.add("a");
		focii.add("the");
		focii.add("all");
		focii.add("any");
		focii.add("every");
		focii.add("no");
		focii.add("other");
		focii.add("some");
		focii.add("such");
		focii.add("that");
		focii.add("this");
		focii.add("I");
		focii.add("he");
		focii.add("you");
		focii.add("who");
		focii.add("and");
		focii.add("because");
		focii.add("but");
		focii.add("or");
		focii.add("if");
		focii.add("though");
		focii.add("while");
		focii.add("how");
		focii.add("when");
		focii.add("where");
		focii.add("why");
		focii.add("again");
		focii.add("ever");
		focii.add("far");
		focii.add("forward");
		focii.add("here");
		focii.add("near");
		focii.add("now");
		focii.add("out");
		focii.add("still");
		focii.add("then");
		focii.add("there");
		focii.add("together");
		focii.add("well");
		focii.add("almost");
		focii.add("enough");
		focii.add("even");
		focii.add("little");
		focii.add("much");
		focii.add("not");
		focii.add("only");
		focii.add("quite");
		focii.add("so");
		focii.add("very");
		focii.add("tomorrow");
		focii.add("yesterday");
		focii.add("north");
		focii.add("south");
		focii.add("east");
		focii.add("west");
		focii.add("please");
		focii.add("yes");
		focii.add("show");
		focii.add("tell");
		focii.add("account");
		focii.add("act");
		focii.add("addition");
		focii.add("adjustment");
		focii.add("advertisement");
		focii.add("agreement");
		focii.add("air");
		focii.add("amount");
		focii.add("amusement");
		focii.add("animal");
		focii.add("answer");
		focii.add("apparatus");
		focii.add("approval");
		focii.add("argument");
		focii.add("art");
		focii.add("attack");
		focii.add("attempt");
		focii.add("attention");
		focii.add("attraction");
		focii.add("authority");
		focii.add("back");
		focii.add("balance");
		focii.add("base");
		focii.add("behaviour");
		focii.add("belief");
		focii.add("birth");
		focii.add("bit");
		focii.add("bite");
		focii.add("blood");
		focii.add("blow");
		focii.add("body");
		focii.add("brass");
		focii.add("bread");
		focii.add("breath");
		focii.add("brother");
		focii.add("building");
		focii.add("burn");
		focii.add("burst");
		focii.add("business");
		focii.add("butter");
		focii.add("canvas");
		focii.add("care");
		focii.add("cause");
		focii.add("chalk");
		focii.add("chance");
		focii.add("change");
		focii.add("cloth");
		focii.add("coal");
		focii.add("colour");
		focii.add("comfort");
		focii.add("committee");
		focii.add("company");
		focii.add("comparison");
		focii.add("competition");
		focii.add("condition");
		focii.add("connection");
		focii.add("control");
		focii.add("cook");
		focii.add("copper");
		focii.add("copy");
		focii.add("cork");
		focii.add("cotton");
		focii.add("cough");
		focii.add("country");
		focii.add("cover");
		focii.add("crack");
		focii.add("credit");
		focii.add("crime");
		focii.add("crush");
		focii.add("cry");
		focii.add("current");
		focii.add("curve");
		focii.add("damage");
		focii.add("danger");
		focii.add("daughter");
		focii.add("day");
		focii.add("death");
		focii.add("debt");
		focii.add("decision");
		focii.add("degree");
		focii.add("design");
		focii.add("desire");
		focii.add("destruction");
		focii.add("detail");
		focii.add("development");
		focii.add("digestion");
		focii.add("direction");
		focii.add("discovery");
		focii.add("discussion");
		focii.add("disease");
		focii.add("disgust");
		focii.add("distance");
		focii.add("distribution");
		focii.add("division");
		focii.add("doubt");
		focii.add("drink");
		focii.add("driving");
		focii.add("dust");
		focii.add("earth");
		focii.add("edge");
		focii.add("education");
		focii.add("effect");
		focii.add("end");
		focii.add("error");
		focii.add("event");
		focii.add("example");
		focii.add("exchange");
		focii.add("existence");
		focii.add("expansion");
		focii.add("experience");
		focii.add("expert");
		focii.add("fact");
		focii.add("fall");
		focii.add("family");
		focii.add("father");
		focii.add("fear");
		focii.add("feeling");
		focii.add("fiction");
		focii.add("field");
		focii.add("fight");
		focii.add("fire");
		focii.add("flame");
		focii.add("flight");
		focii.add("flower");
		focii.add("fold");
		focii.add("food");
		focii.add("force");
		focii.add("form");
		focii.add("friend");
		focii.add("front");
		focii.add("fruit");
		focii.add("glass");
		focii.add("gold");
		focii.add("government");
		focii.add("grain");
		focii.add("grass");
		focii.add("grip");
		focii.add("group");
		focii.add("growth");
		focii.add("guide");
		focii.add("harbour");
		focii.add("harmony");
		focii.add("hate");
		focii.add("hearing");
		focii.add("heat");
		focii.add("help");
		focii.add("history");
		focii.add("hole");
		focii.add("hope");
		focii.add("hour");
		focii.add("humour");
		focii.add("ice");
		focii.add("idea");
		focii.add("impulse");
		focii.add("increase");
		focii.add("industry");
		focii.add("ink");
		focii.add("insect");
		focii.add("instrument");
		focii.add("insurance");
		focii.add("interest");
		focii.add("invention");
		focii.add("iron");
		focii.add("jelly");
		focii.add("join");
		focii.add("journey");
		focii.add("judge");
		focii.add("jump");
		focii.add("kick");
		focii.add("kiss");
		focii.add("knowledge");
		focii.add("land");
		focii.add("language");
		focii.add("laugh");
		focii.add("law");
		focii.add("lead");
		focii.add("learning");
		focii.add("leather");
		focii.add("letter");
		focii.add("level");
		focii.add("lift");
		focii.add("light");
		focii.add("limit");
		focii.add("linen");
		focii.add("liquid");
		focii.add("list");
		focii.add("look");
		focii.add("loss");
		focii.add("love");
		focii.add("machine");
		focii.add("man");
		focii.add("manager");
		focii.add("mark");
		focii.add("market");
		focii.add("mass");
		focii.add("meal");
		focii.add("measure");
		focii.add("meat");
		focii.add("meeting");
		focii.add("memory");
		focii.add("metal");
		focii.add("middle");
		focii.add("milk");
		focii.add("mind");
		focii.add("mine");
		focii.add("minute");
		focii.add("mist");
		focii.add("money");
		focii.add("month");
		focii.add("morning");
		focii.add("mother");
		focii.add("motion");
		focii.add("mountain");
		focii.add("move");
		focii.add("music");
		focii.add("name");
		focii.add("nation");
		focii.add("need");
		focii.add("news");
		focii.add("night");
		focii.add("noise");
		focii.add("note");
		focii.add("number");
		focii.add("observation");
		focii.add("offer");
		focii.add("oil");
		focii.add("operation");
		focii.add("opinion");
		focii.add("order");
		focii.add("organization");
		focii.add("ornament");
		focii.add("owner");
		focii.add("page");
		focii.add("pain");
		focii.add("paint");
		focii.add("paper");
		focii.add("part");
		focii.add("paste");
		focii.add("payment");
		focii.add("peace");
		focii.add("person");
		focii.add("place");
		focii.add("plant");
		focii.add("play");
		focii.add("pleasure");
		focii.add("point");
		focii.add("poison");
		focii.add("polish");
		focii.add("porter");
		focii.add("position");
		focii.add("powder");
		focii.add("power");
		focii.add("price");
		focii.add("print");
		focii.add("process");
		focii.add("produce");
		focii.add("profit");
		focii.add("property");
		focii.add("prose");
		focii.add("protest");
		focii.add("pull");
		focii.add("punishment");
		focii.add("purpose");
		focii.add("push");
		focii.add("quality");
		focii.add("question");
		focii.add("rain");
		focii.add("range");
		focii.add("rate");
		focii.add("ray");
		focii.add("reaction");
		focii.add("reading");
		focii.add("reason");
		focii.add("record");
		focii.add("regret");
		focii.add("relation");
		focii.add("religion");
		focii.add("representative");
		focii.add("request");
		focii.add("respect");
		focii.add("rest");
		focii.add("reward");
		focii.add("rhythm");
		focii.add("rice");
		focii.add("river");
		focii.add("road");
		focii.add("roll");
		focii.add("room");
		focii.add("rub");
		focii.add("rule");
		focii.add("run");
		focii.add("salt");
		focii.add("sand");
		focii.add("scale");
		focii.add("science");
		focii.add("sea");
		focii.add("seat");
		focii.add("secretary");
		focii.add("selection");
		focii.add("self");
		focii.add("sense");
		focii.add("servant");
		focii.add("sex");
		focii.add("shade");
		focii.add("shake");
		focii.add("shame");
		focii.add("shock");
		focii.add("side");
		focii.add("sign");
		focii.add("silk");
		focii.add("silver");
		focii.add("sister");
		focii.add("size");
		focii.add("sky");
		focii.add("sleep");
		focii.add("slip");
		focii.add("slope");
		focii.add("smash");
		focii.add("smell");
		focii.add("smile");
		focii.add("smoke");
		focii.add("sneeze");
		focii.add("snow");
		focii.add("soap");
		focii.add("society");
		focii.add("son");
		focii.add("song");
		focii.add("sort");
		focii.add("sound");
		focii.add("soup");
		focii.add("space");
		focii.add("stage");
		focii.add("start");
		focii.add("statement");
		focii.add("steam");
		focii.add("steel");
		focii.add("step");
		focii.add("stitch");
		focii.add("stone");
		focii.add("stop");
		focii.add("story");
		focii.add("stretch");
		focii.add("structure");
		focii.add("substance");
		focii.add("sugar");
		focii.add("suggestion");
		focii.add("summer");
		focii.add("support");
		focii.add("surprise");
		focii.add("swim");
		focii.add("system");
		focii.add("talk");
		focii.add("taste");
		focii.add("tax");
		focii.add("teaching");
		focii.add("tendency");
		focii.add("test");
		focii.add("theory");
		focii.add("thing");
		focii.add("thought");
		focii.add("thunder");
		focii.add("time");
		focii.add("tin");
		focii.add("top");
		focii.add("touch");
		focii.add("trade");
		focii.add("transport");
		focii.add("trick");
		focii.add("trouble");
		focii.add("turn");
		focii.add("twist");
		focii.add("unit");
		focii.add("use");
		focii.add("value");
		focii.add("verse");
		focii.add("vessel");
		focii.add("view");
		focii.add("voice");
		focii.add("walk");
		focii.add("war");
		focii.add("wash");
		focii.add("waste");
		focii.add("water");
		focii.add("wave");
		focii.add("wax");
		focii.add("way");
		focii.add("weather");
		focii.add("week");
		focii.add("weight");
		focii.add("wind");
		focii.add("wine");
		focii.add("winter");
		focii.add("woman");
		focii.add("wood");
		focii.add("wool");
		focii.add("word");
		focii.add("work");
		focii.add("wound");
		focii.add("writing");
		focii.add("year");
		focii.add("angle");
		focii.add("ant");
		focii.add("apple");
		focii.add("arch");
		focii.add("arm");
		focii.add("army");
		focii.add("baby");
		focii.add("bag");
		focii.add("ball");
		focii.add("band");
		focii.add("basin");
		focii.add("basket");
		focii.add("bath");
		focii.add("bed");
		focii.add("bee");
		focii.add("bell");
		focii.add("berry");
		focii.add("bird");
		focii.add("blade");
		focii.add("board");
		focii.add("boat");
		focii.add("bone");
		focii.add("book");
		focii.add("boot");
		focii.add("bottle");
		focii.add("box");
		focii.add("boy");
		focii.add("brain");
		focii.add("brake");
		focii.add("branch");
		focii.add("brick");
		focii.add("bridge");
		focii.add("brush");
		focii.add("bucket");
		focii.add("bulb");
		focii.add("button");
		focii.add("cake");
		focii.add("camera");
		focii.add("card");
		focii.add("cart");
		focii.add("carriage");
		focii.add("cat");
		focii.add("chain");
		focii.add("cheese");
		focii.add("chest");
		focii.add("chin");
		focii.add("church");
		focii.add("circle");
		focii.add("clock");
		focii.add("cloud");
		focii.add("coat");
		focii.add("collar");
		focii.add("comb");
		focii.add("cord");
		focii.add("cow");
		focii.add("cup");
		focii.add("curtain");
		focii.add("cushion");
		focii.add("dog");
		focii.add("door");
		focii.add("drain");
		focii.add("drawer");
		focii.add("dress");
		focii.add("drop");
		focii.add("ear");
		focii.add("egg");
		focii.add("engine");
		focii.add("eye");
		focii.add("face");
		focii.add("farm");
		focii.add("feather");
		focii.add("finger");
		focii.add("fish");
		focii.add("flag");
		focii.add("floor");
		focii.add("fly");
		focii.add("foot");
		focii.add("fork");
		focii.add("fowl");
		focii.add("frame");
		focii.add("garden");
		focii.add("girl");
		focii.add("glove");
		focii.add("goat");
		focii.add("gun");
		focii.add("hair");
		focii.add("hammer");
		focii.add("hand");
		focii.add("hat");
		focii.add("head");
		focii.add("heart");
		focii.add("hook");
		focii.add("horn");
		focii.add("horse");
		focii.add("hospital");
		focii.add("house");
		focii.add("island");
		focii.add("jewel");
		focii.add("kettle");
		focii.add("key");
		focii.add("knee");
		focii.add("knife");
		focii.add("knot");
		focii.add("leaf");
		focii.add("leg");
		focii.add("library");
		focii.add("line");
		focii.add("lip");
		focii.add("lock");
		focii.add("map");
		focii.add("match");
		focii.add("monkey");
		focii.add("moon");
		focii.add("mouth");
		focii.add("muscle");
		focii.add("nail");
		focii.add("neck");
		focii.add("needle");
		focii.add("nerve");
		focii.add("net");
		focii.add("nose");
		focii.add("nut");
		focii.add("office");
		focii.add("orange");
		focii.add("oven");
		focii.add("parcel");
		focii.add("pen");
		focii.add("pencil");
		focii.add("picture");
		focii.add("pig");
		focii.add("pin");
		focii.add("pipe");
		focii.add("plane");
		focii.add("plate");
		focii.add("plough");
		focii.add("pocket");
		focii.add("pot");
		focii.add("potato");
		focii.add("prison");
		focii.add("pump");
		focii.add("rail");
		focii.add("rat");
		focii.add("receipt");
		focii.add("ring");
		focii.add("rod");
		focii.add("roof");
		focii.add("root");
		focii.add("sail");
		focii.add("school");
		focii.add("scissors");
		focii.add("screw");
		focii.add("seed");
		focii.add("sheep");
		focii.add("shelf");
		focii.add("ship");
		focii.add("shirt");
		focii.add("shoe");
		focii.add("skin");
		focii.add("skirt");
		focii.add("snake");
		focii.add("sock");
		focii.add("spade");
		focii.add("sponge");
		focii.add("spoon");
		focii.add("spring");
		focii.add("square");
		focii.add("stamp");
		focii.add("star");
		focii.add("station");
		focii.add("stem");
		focii.add("stick");
		focii.add("stocking");
		focii.add("stomach");
		focii.add("store");
		focii.add("street");
		focii.add("sun");
		focii.add("table");
		focii.add("tail");
		focii.add("thread");
		focii.add("throat");
		focii.add("thumb");
		focii.add("ticket");
		focii.add("toe");
		focii.add("tongue");
		focii.add("tooth");
		focii.add("town");
		focii.add("train");
		focii.add("tray");
		focii.add("tree");
		focii.add("trousers");
		focii.add("umbrella");
		focii.add("wall");
		focii.add("watch");
		focii.add("wheel");
		focii.add("whip");
		focii.add("whistle");
		focii.add("window");
		focii.add("wing");
		focii.add("wire");
		focii.add("worm");
		focii.add("able");
		focii.add("acid");
		focii.add("angry");
		focii.add("automatic");
		focii.add("beautiful");
		focii.add("black");
		focii.add("boiling");
		focii.add("bright");
		focii.add("broken");
		focii.add("brown");
		focii.add("cheap");
		focii.add("chemical");
		focii.add("chief");
		focii.add("clean");
		focii.add("clear");
		focii.add("common");
		focii.add("complex");
		focii.add("conscious");
		focii.add("cut");
		focii.add("deep");
		focii.add("dependent");
		focii.add("early");
		focii.add("elastic");
		focii.add("electric");
		focii.add("equal");
		focii.add("fat");
		focii.add("fertile");
		focii.add("first");
		focii.add("fixed");
		focii.add("flat");
		focii.add("free");
		focii.add("frequent");
		focii.add("full");
		focii.add("general");
		focii.add("good");
		focii.add("great");
		focii.add("grey");
		focii.add("hanging");
		focii.add("happy");
		focii.add("hard");
		focii.add("healthy");
		focii.add("high");
		focii.add("hollow");
		focii.add("important");
		focii.add("kind");
		focii.add("like");
		focii.add("living");
		focii.add("long");
		focii.add("male");
		focii.add("married");
		focii.add("material");
		focii.add("medical");
		focii.add("military");
		focii.add("natural");
		focii.add("necessary");
		focii.add("new");
		focii.add("normal");
		focii.add("open");
		focii.add("parallel");
		focii.add("past");
		focii.add("physical");
		focii.add("political");
		focii.add("poor");
		focii.add("possible");
		focii.add("present");
		focii.add("private");
		focii.add("probable");
		focii.add("quick");
		focii.add("quiet");
		focii.add("ready");
		focii.add("red");
		focii.add("regular");
		focii.add("responsible");
		focii.add("right");
		focii.add("round");
		focii.add("same");
		focii.add("second");
		focii.add("separate");
		focii.add("serious");
		focii.add("sharp");
		focii.add("smooth");
		focii.add("sticky");
		focii.add("stiff");
		focii.add("straight");
		focii.add("strong");
		focii.add("sudden");
		focii.add("sweet");
		focii.add("tall");
		focii.add("thick");
		focii.add("tight");
		focii.add("tired");
		focii.add("true");
		focii.add("violent");
		focii.add("waiting");
		focii.add("warm");
		focii.add("wet");
		focii.add("wide");
		focii.add("wise");
		focii.add("yellow");
		focii.add("young");
		focii.add("awake");
		focii.add("bad");
		focii.add("bent");
		focii.add("bitter");
		focii.add("blue");
		focii.add("certain");
		focii.add("cold");
		focii.add("complete");
		focii.add("cruel");
		focii.add("dark");
		focii.add("dead");
		focii.add("dear");
		focii.add("delicate");
		focii.add("different");
		focii.add("dirty");
		focii.add("dry");
		focii.add("false");
		focii.add("feeble");
		focii.add("female");
		focii.add("foolish");
		focii.add("future");
		focii.add("green");
		focii.add("ill");
		focii.add("last");
		focii.add("late");
		focii.add("left");
		focii.add("loose");
		focii.add("loud");
		focii.add("low");
		focii.add("mixed");
		focii.add("narrow");
		focii.add("old");
		focii.add("opposite");
		focii.add("public");
		focii.add("rough");
		focii.add("sad");
		focii.add("safe");
		focii.add("secret");
		focii.add("short");
		focii.add("shut");
		focii.add("simple");
		focii.add("slow");
		focii.add("small");
		focii.add("soft");
		focii.add("solid");
		focii.add("special");
		focii.add("strange");
		focii.add("thin");
		focii.add("white");
		focii.add("wrong");
	}

	public void addLiebermanWords() {
		focii.add("victory");
		focii.add("fitness");
		focii.add("inspiration");
		focii.add("friendship");
		focii.add("applause");
		focii.add("contentment");
		focii.add("headache");
		focii.add("fame");
		focii.add("appreciation");
		focii.add("experience");
		focii.add("luxury");
		focii.add("achievement");
		focii.add("fulfillment");
		focii.add("prestige");
		focii.add("success");
		focii.add("network");
		focii.add("relief");
		focii.add("honor");
		focii.add("encouragement");
		focii.add("popularity");
		focii.add("companionship");
		focii.add("solution");
		focii.add("neat");
		focii.add("strength");
		focii.add("preference");
		focii.add("creativity");
		focii.add("promotion");
		focii.add("convenience");
		focii.add("wisdom");
		focii.add("affection");
		focii.add("interaction");
		focii.add("winner");
		focii.add("bliss");
		focii.add("self-esteem");
		focii.add("diversity");
		focii.add("partner");
		focii.add("commitment");
		focii.add("novelty");
		focii.add("proof");
		focii.add("harmony");
		focii.add("compassion");
		focii.add("glory");
		focii.add("detail");
		focii.add("talent");
		focii.add("identity");
		focii.add("nanotechnology");
		focii.add("congratulation");
		focii.add("recognition");
		focii.add("gratification");
		focii.add("explanation");
		focii.add("goal");
		focii.add("evidence");
		focii.add("wise");
		focii.add("compliment");
		focii.add("adventure");
		focii.add("courage");
		focii.add("technology");
		focii.add("equality");
		focii.add("liberty");
		focii.add("adoration");
		focii.add("variety");
		focii.add("passion");
		focii.add("fortune");
		focii.add("generosity");
		focii.add("admiration");
		focii.add("tranquillity");
		focii.add("longevity");
		focii.add("purpose");
		focii.add("champion");
		focii.add("justice");
		focii.add("approval");
		focii.add("sanity");
		focii.add("intelligence");
		focii.add("acceptance");
		focii.add("wealth");
		focii.add("acknowledgment");
		focii.add("communication");
		focii.add("forgiveness");
		focii.add("sympathy");
		focii.add("confidence");
		focii.add("ambition");
		focii.add("romance");
		focii.add("wellness");
		focii.add("integrity");
		focii.add("dedication");
		focii.add("closure");
		focii.add("discount");
		focii.add("dialogue");
		focii.add("saving");
		focii.add("consistency");
		focii.add("reliability");
		focii.add("esteem");
		focii.add("dignity");
		focii.add("motivation");
		focii.add("option");
		focii.add("luck");
		focii.add("belief");
		focii.add("clarity");
		focii.add("complement");
		focii.add("certainty");
		focii.add("remission");
		focii.add("example");
		focii.add("simplicity");
		focii.add("ridicule");
		focii.add("choice");
		focii.add("independence");
		focii.add("prosperity");
		focii.add("influence");
		focii.add("nourishment");
		focii.add("agreement");
		focii.add("mango");
		focii.add("devotion");
		focii.add("opportunity");
		focii.add("stability");
		focii.add("nicotine");
		focii.add("quality");
		focii.add("massage");
		focii.add("spouse");
		focii.add("speed");
		focii.add("salvation");
		focii.add("link");
		focii.add("kindness");
		focii.add("kind");
		focii.add("pretzel");
		focii.add("mystery");
		focii.add("satisfaction");
		focii.add("treasure");
		focii.add("spirituality");
		focii.add("loyalty");
		focii.add("hip");
		focii.add("privacy");
		focii.add("companion");
		focii.add("gift");
		focii.add("hobby");
		focii.add("bargain");
		focii.add("ice cream");
		focii.add("hero");
		focii.add("honesty");
		focii.add("everything");
		focii.add("taco");
		focii.add("sight");
		focii.add("diamond");
		focii.add("tobacco");
		focii.add("growth");
		focii.add("present");
		focii.add("answer");
		focii.add("sunlight");
		focii.add("safety");
		focii.add("valor");
		focii.add("amusement");
		focii.add("warmth");
		focii.add("parent");
		focii.add("jewelry");
		focii.add("limb");
		focii.add("fact");
		focii.add("freedom");
		focii.add("brain");
		focii.add("lightning");
		focii.add("peanut");
		focii.add("lemonade");
		focii.add("newscast");
		focii.add("part");
		focii.add("stable");
		focii.add("PDA");
		focii.add("lover");
		focii.add("king");
		focii.add("patience");
		focii.add("security");
		focii.add("mom");
		focii.add("excitement");
		focii.add("gold");
		focii.add("hairstyle");
		focii.add("joy");
		focii.add("quiet");
		focii.add("name");
		focii.add("residence");
		focii.add("leader");
		focii.add("sunshine");
		focii.add("leisure");
		focii.add("menstruation");
		focii.add("time");
		focii.add("laughter");
		focii.add("health");
		focii.add("life");
		focii.add("emancipation");
		focii.add("advantage");
		focii.add("value");
		focii.add("pacifism");
		focii.add("significance");
		focii.add("energy");
		focii.add("totality");
		focii.add("hamburger");
		focii.add("care");
		focii.add("snack");
		focii.add("chivalry");
		focii.add("riches");
		focii.add("tail");
		focii.add("haircut");
		focii.add("friend");
		focii.add("excellence");
		focii.add("sandwich");
		focii.add("smash");
		focii.add("laud");
		focii.add("bounty");
		focii.add("holiday");
		focii.add("coffer");
		focii.add("education");
		focii.add("property");
		focii.add("tine");
		focii.add("noodle");
		focii.add("slogan");
		focii.add("relationship");
		focii.add("science");
		focii.add("information");
		focii.add("company");
		focii.add("breakfast");
		focii.add("oxygen");
		focii.add("bonus");
		focii.add("emotion");
		focii.add("muscle");
		focii.add("relaxation");
		focii.add("preparedness");
		focii.add("maintenance");
		focii.add("benefit");
		focii.add("candy");
		focii.add("beef");
		focii.add("polymer");
		focii.add("atheism");
		focii.add("decade");
		focii.add("space");
		focii.add("knowledge");
		focii.add("lobster");
		focii.add("prior");
		focii.add("bagel");
		focii.add("accolade");
		focii.add("limelight");
		focii.add("instant");
		focii.add("reinforcement");
		focii.add("self-confidence");
		focii.add("surprise");
		focii.add("lot");
		focii.add("outer space");
		focii.add("art");
		focii.add("ethic");
		focii.add("hair");
		focii.add("dessert");
		focii.add("magic");
		focii.add("assurance");
		focii.add("opal");
		focii.add("need");
		focii.add("light");
		focii.add("amour");
		focii.add("vacation");

	}

	public void processSignal(Object signal) {
		if (signal instanceof BetterSignal) {

			BetterSignal s = (BetterSignal) signal;
			Sequence story = s.get(0, Sequence.class);
			// Sequence explicitElements = s.get(1, Sequence.class);
			// Sequence inferences = s.get(2, Sequence.class);
			// Sequence concepts = s.get(3, Sequence.class);
			processStory(story);
		}
	}

	private void processStory(Sequence story) {
		if (!Switch.countConceptNetWords.isSelected()) {
			// Mark.say("Not running Entity Expert");
			return;
		}

		types.clear();
		Mark.say("\n\n\nStory elements");
		// for (Entity e : story.getElements()) {
		// Mark.say(e.asString());
		// }
		discoverTypes(story);
		int count = types.size();
		types.retainAll(focii);
		Mark.say("There are", focii.size(), "good words,", count, "words", "in", story.getType(), "yielding ", types.size(), " words in both");
		for (String s : types) {
			Mark.say(">>", s);
		}
		System.out.println("\n\n");
	}

	private void discoverTypes(Entity e) {

		if (e.entityP()) {
			types.add(e.getType());
		}
		else if (e.functionP()) {
			types.add(e.getType());
			// Recurse
			discoverTypes(e.getSubject());
		}
		else if (e.relationP()) {
			if (!e.getType().equals("property")) {
				types.add(e.getType());
			}
			// Recurse
			discoverTypes(e.getSubject());
			discoverTypes(e.getObject());
		}
		else {
			types.add(e.getType());
			for (Entity x : e.getElements()) {
				discoverTypes(x);
			}
		}

	}

}