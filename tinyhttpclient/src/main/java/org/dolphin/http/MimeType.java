package org.dolphin.http;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import hyn.com.lib.Preconditions;
import hyn.com.lib.ValueUtil;

import static com.hanyanan.http.HttpUtil.CONNECTOR;

/**
 * Created by hanyanan on 2015/5/16.
 */
public class MimeType {
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    public static final String DEFAULT_URL_MIME_TYPE = "application/x-www-form-urlencoded";
    public static final String DEFAULT_CHARSET = "charset=utf-8";

    public static final String APPLICATION_TYPE = "application";
    public static final String AUDIO_TYPE = "audio";
    public static final String IMAGE_TYPE = "image";
    public static final String TEXT_TYPE = "text";
    public static final String VIDEO_TYPE = "video";

    private static final String WILDCARD = "*";


    public static final Map<String, String> sMimeTypeMap = new HashMap<String, String>();

    static {
        sMimeTypeMap.put("ez", "application/andrew-inset");
        sMimeTypeMap.put("aw", "application/applixware");
        sMimeTypeMap.put("atom", "application/atom+xml");
        sMimeTypeMap.put("atomcat", "application/atomcat+xml");
        sMimeTypeMap.put("atomsvc", "application/atomsvc+xml");
        sMimeTypeMap.put("ccxml", "application/ccxml+xml");
        sMimeTypeMap.put("cdmia", "application/cdmi-capability");
        sMimeTypeMap.put("cdmic", "application/cdmi-container");
        sMimeTypeMap.put("cdmid", "application/cdmi-domain");
        sMimeTypeMap.put("cdmio", "application/cdmi-object");
        sMimeTypeMap.put("cdmiq", "application/cdmi-queue");
        sMimeTypeMap.put("cu", "application/cu-seeme");
        sMimeTypeMap.put("davmount", "application/davmount+xml");
        sMimeTypeMap.put("dbk", "application/docbook+xml");
        sMimeTypeMap.put("dssc", "application/dssc+der");
        sMimeTypeMap.put("xdssc", "application/dssc+xml");
        sMimeTypeMap.put("ecma", "application/ecmascript");
        sMimeTypeMap.put("emma", "application/emma+xml");
        sMimeTypeMap.put("epub", "application/epub+zip");
        sMimeTypeMap.put("exi", "application/exi");
        sMimeTypeMap.put("pfr", "application/font-tdpfr");
        sMimeTypeMap.put("gml", "application/gml+xml");
        sMimeTypeMap.put("gpx", "application/gpx+xml");
        sMimeTypeMap.put("gxf", "application/gxf");
        sMimeTypeMap.put("stk", "application/hyperstudio");
        sMimeTypeMap.put("inkml", "application/inkml+xml");
        sMimeTypeMap.put("ink", "application/inkml+xml");
        sMimeTypeMap.put("ipfix", "application/ipfix");
        sMimeTypeMap.put("jar", "application/java-archive");
        sMimeTypeMap.put("ser", "application/java-serialized-object");
        sMimeTypeMap.put("class", "application/java-vm");
        sMimeTypeMap.put("js", "application/javascript");
        sMimeTypeMap.put("json", "application/json");
        sMimeTypeMap.put("jsonml", "application/jsonml+json");
        sMimeTypeMap.put("lostxml", "application/lost+xml");
        sMimeTypeMap.put("hqx", "application/mac-binhex40");
        sMimeTypeMap.put("cpt", "application/mac-compactpro");
        sMimeTypeMap.put("mads", "application/mads+xml");
        sMimeTypeMap.put("mrc", "application/marc");
        sMimeTypeMap.put("mrcx", "application/marcxml+xml");
        sMimeTypeMap.put("mb", "application/mathematica");
        sMimeTypeMap.put("nb", "application/mathematica");
        sMimeTypeMap.put("ma", "application/mathematica");
        sMimeTypeMap.put("mathml", "application/mathml+xml");
        sMimeTypeMap.put("mbox", "application/mbox");
        sMimeTypeMap.put("mscml", "application/mediaservercontrol+xml");
        sMimeTypeMap.put("metalink", "application/metalink+xml");
        sMimeTypeMap.put("meta4", "application/metalink4+xml");
        sMimeTypeMap.put("mets", "application/mets+xml");
        sMimeTypeMap.put("mods", "application/mods+xml");
        sMimeTypeMap.put("mp21", "application/mp21");
        sMimeTypeMap.put("m21", "application/mp21");
        sMimeTypeMap.put("mp4s", "application/mp4");
        sMimeTypeMap.put("dot", "application/msword");
        sMimeTypeMap.put("doc", "application/msword");
        sMimeTypeMap.put("mxf", "application/mxf");
        sMimeTypeMap.put("deploy", "application/octet-stream");
        sMimeTypeMap.put("elc", "application/octet-stream");
        sMimeTypeMap.put("dump", "application/octet-stream");
        sMimeTypeMap.put("bpk", "application/octet-stream");
        sMimeTypeMap.put("pkg", "application/octet-stream");
        sMimeTypeMap.put("distz", "application/octet-stream");
        sMimeTypeMap.put("dist", "application/octet-stream");
        sMimeTypeMap.put("so", "application/octet-stream");
        sMimeTypeMap.put("mar", "application/octet-stream");
        sMimeTypeMap.put("lrf", "application/octet-stream");
        sMimeTypeMap.put("dms", "application/octet-stream");
        sMimeTypeMap.put("bin", "application/octet-stream");
        sMimeTypeMap.put("oda", "application/oda");
        sMimeTypeMap.put("opf", "application/oebps-package+xml");
        sMimeTypeMap.put("ogx", "application/ogg");
        sMimeTypeMap.put("omdoc", "application/omdoc+xml");
        sMimeTypeMap.put("onepkg", "application/onenote");
        sMimeTypeMap.put("onetmp", "application/onenote");
        sMimeTypeMap.put("onetoc2", "application/onenote");
        sMimeTypeMap.put("onetoc", "application/onenote");
        sMimeTypeMap.put("oxps", "application/oxps");
        sMimeTypeMap.put("xer", "application/patch-ops-error+xml");
        sMimeTypeMap.put("pdf", "application/pdf");
        sMimeTypeMap.put("pgp", "application/pgp-encrypted");
        sMimeTypeMap.put("sig", "application/pgp-signature");
        sMimeTypeMap.put("asc", "application/pgp-signature");
        sMimeTypeMap.put("prf", "application/pics-rules");
        sMimeTypeMap.put("p10", "application/pkcs10");
        sMimeTypeMap.put("p7c", "application/pkcs7-mime");
        sMimeTypeMap.put("p7m", "application/pkcs7-mime");
        sMimeTypeMap.put("p7s", "application/pkcs7-signature");
        sMimeTypeMap.put("p8", "application/pkcs8");
        sMimeTypeMap.put("ac", "application/pkix-attr-cert");
        sMimeTypeMap.put("cer", "application/pkix-cert");
        sMimeTypeMap.put("crl", "application/pkix-crl");
        sMimeTypeMap.put("pkipath", "application/pkix-pkipath");
        sMimeTypeMap.put("pki", "application/pkixcmp");
        sMimeTypeMap.put("pls", "application/pls+xml");
        sMimeTypeMap.put("ps", "application/postscript");
        sMimeTypeMap.put("eps", "application/postscript");
        sMimeTypeMap.put("ai", "application/postscript");
        sMimeTypeMap.put("cww", "application/prs.cww");
        sMimeTypeMap.put("pskcxml", "application/pskc+xml");
        sMimeTypeMap.put("rdf", "application/rdf+xml");
        sMimeTypeMap.put("rif", "application/reginfo+xml");
        sMimeTypeMap.put("rnc", "application/relax-ng-compact-syntax");
        sMimeTypeMap.put("rl", "application/resource-lists+xml");
        sMimeTypeMap.put("rld", "application/resource-lists-diff+xml");
        sMimeTypeMap.put("rs", "application/rls-services+xml");
        sMimeTypeMap.put("gbr", "application/rpki-ghostbusters");
        sMimeTypeMap.put("mft", "application/rpki-manifest");
        sMimeTypeMap.put("roa", "application/rpki-roa");
        sMimeTypeMap.put("rsd", "application/rsd+xml");
        sMimeTypeMap.put("rss", "application/rss+xml");
        sMimeTypeMap.put("rtf", "application/rtf");
        sMimeTypeMap.put("sbml", "application/sbml+xml");
        sMimeTypeMap.put("scq", "application/scvp-cv-request");
        sMimeTypeMap.put("scs", "application/scvp-cv-response");
        sMimeTypeMap.put("spq", "application/scvp-vp-request");
        sMimeTypeMap.put("spp", "application/scvp-vp-response");
        sMimeTypeMap.put("sdp", "application/sdp");
        sMimeTypeMap.put("setpay", "application/set-payment-initiation");
        sMimeTypeMap.put("setreg", "application/set-registration-initiation");
        sMimeTypeMap.put("shf", "application/shf+xml");
        sMimeTypeMap.put("smil", "application/smil+xml");
        sMimeTypeMap.put("smi", "application/smil+xml");
        sMimeTypeMap.put("rq", "application/sparql-query");
        sMimeTypeMap.put("srx", "application/sparql-results+xml");
        sMimeTypeMap.put("gram", "application/srgs");
        sMimeTypeMap.put("grxml", "application/srgs+xml");
        sMimeTypeMap.put("sru", "application/sru+xml");
        sMimeTypeMap.put("ssdl", "application/ssdl+xml");
        sMimeTypeMap.put("ssml", "application/ssml+xml");
        sMimeTypeMap.put("teicorpus", "application/tei+xml");
        sMimeTypeMap.put("tei", "application/tei+xml");
        sMimeTypeMap.put("tfi", "application/thraud+xml");
        sMimeTypeMap.put("tsd", "application/timestamped-data");
        sMimeTypeMap.put("plb", "application/vnd.3gpp.pic-bw-large");
        sMimeTypeMap.put("psb", "application/vnd.3gpp.pic-bw-small");
        sMimeTypeMap.put("pvb", "application/vnd.3gpp.pic-bw-var");
        sMimeTypeMap.put("tcap", "application/vnd.3gpp2.tcap");
        sMimeTypeMap.put("pwn", "application/vnd.3m.post-it-notes");
        sMimeTypeMap.put("aso", "application/vnd.accpac.simply.aso");
        sMimeTypeMap.put("imp", "application/vnd.accpac.simply.imp");
        sMimeTypeMap.put("acu", "application/vnd.acucobol");
        sMimeTypeMap.put("acutc", "application/vnd.acucorp");
        sMimeTypeMap.put("atc", "application/vnd.acucorp");
        sMimeTypeMap.put("air", "application/vnd.adobe.air-application-installer-package+zip");
        sMimeTypeMap.put("fcdt", "application/vnd.adobe.formscentral.fcdt");
        sMimeTypeMap.put("fxpl", "application/vnd.adobe.fxp");
        sMimeTypeMap.put("fxp", "application/vnd.adobe.fxp");
        sMimeTypeMap.put("xdp", "application/vnd.adobe.xdp+xml");
        sMimeTypeMap.put("xfdf", "application/vnd.adobe.xfdf");
        sMimeTypeMap.put("ahead", "application/vnd.ahead.space");
        sMimeTypeMap.put("azf", "application/vnd.airzip.filesecure.azf");
        sMimeTypeMap.put("azs", "application/vnd.airzip.filesecure.azs");
        sMimeTypeMap.put("azw", "application/vnd.amazon.ebook");
        sMimeTypeMap.put("acc", "application/vnd.americandynamics.acc");
        sMimeTypeMap.put("ami", "application/vnd.amiga.ami");
        sMimeTypeMap.put("apk", "application/vnd.android.package-archive");
        sMimeTypeMap.put("cii", "application/vnd.anser-web-certificate-issue-initiation");
        sMimeTypeMap.put("fti", "application/vnd.anser-web-funds-transfer-initiation");
        sMimeTypeMap.put("atx", "application/vnd.antix.game-component");
        sMimeTypeMap.put("mpkg", "application/vnd.apple.installer+xml");
        sMimeTypeMap.put("m3u8", "application/vnd.apple.mpegurl");
        sMimeTypeMap.put("swi", "application/vnd.aristanetworks.swi");
        sMimeTypeMap.put("iota", "application/vnd.astraea-software.iota");
        sMimeTypeMap.put("aep", "application/vnd.audiograph");
        sMimeTypeMap.put("mpm", "application/vnd.blueice.multipass");
        sMimeTypeMap.put("bmi", "application/vnd.bmi");
        sMimeTypeMap.put("rep", "application/vnd.businessobjects");
        sMimeTypeMap.put("cdxml", "application/vnd.chemdraw+xml");
        sMimeTypeMap.put("mmd", "application/vnd.chipnuts.karaoke-mmd");
        sMimeTypeMap.put("cdy", "application/vnd.cinderella");
        sMimeTypeMap.put("cla", "application/vnd.claymore");
        sMimeTypeMap.put("rp9", "application/vnd.cloanto.rp9");
        sMimeTypeMap.put("c4u", "application/vnd.clonk.c4group");
        sMimeTypeMap.put("c4p", "application/vnd.clonk.c4group");
        sMimeTypeMap.put("c4f", "application/vnd.clonk.c4group");
        sMimeTypeMap.put("c4d", "application/vnd.clonk.c4group");
        sMimeTypeMap.put("c4g", "application/vnd.clonk.c4group");
        sMimeTypeMap.put("c11amc", "application/vnd.cluetrust.cartomobile-config");
        sMimeTypeMap.put("c11amz", "application/vnd.cluetrust.cartomobile-config-pkg");
        sMimeTypeMap.put("csp", "application/vnd.commonspace");
        sMimeTypeMap.put("cdbcmsg", "application/vnd.contact.cmsg");
        sMimeTypeMap.put("cmc", "application/vnd.cosmocaller");
        sMimeTypeMap.put("clkx", "application/vnd.crick.clicker");
        sMimeTypeMap.put("clkk", "application/vnd.crick.clicker.keyboard");
        sMimeTypeMap.put("clkp", "application/vnd.crick.clicker.palette");
        sMimeTypeMap.put("clkt", "application/vnd.crick.clicker.template");
        sMimeTypeMap.put("clkw", "application/vnd.crick.clicker.wordbank");
        sMimeTypeMap.put("wbs", "application/vnd.criticaltools.wbs+xml");
        sMimeTypeMap.put("pml", "application/vnd.ctc-posml");
        sMimeTypeMap.put("ppd", "application/vnd.cups-ppd");
        sMimeTypeMap.put("car", "application/vnd.curl.car");
        sMimeTypeMap.put("pcurl", "application/vnd.curl.pcurl");
        sMimeTypeMap.put("dart", "application/vnd.dart");
        sMimeTypeMap.put("rdz", "application/vnd.data-vision.rdz");
        sMimeTypeMap.put("uvvd", "application/vnd.dece.data");
        sMimeTypeMap.put("uvd", "application/vnd.dece.data");
        sMimeTypeMap.put("uvvf", "application/vnd.dece.data");
        sMimeTypeMap.put("uvf", "application/vnd.dece.data");
        sMimeTypeMap.put("uvvt", "application/vnd.dece.ttml+xml");
        sMimeTypeMap.put("uvt", "application/vnd.dece.ttml+xml");
        sMimeTypeMap.put("uvvx", "application/vnd.dece.unspecified");
        sMimeTypeMap.put("uvx", "application/vnd.dece.unspecified");
        sMimeTypeMap.put("uvvz", "application/vnd.dece.zip");
        sMimeTypeMap.put("uvz", "application/vnd.dece.zip");
        sMimeTypeMap.put("fe_launch", "application/vnd.denovo.fcselayout-link");
        sMimeTypeMap.put("dna", "application/vnd.dna");
        sMimeTypeMap.put("mlp", "application/vnd.dolby.mlp");
        sMimeTypeMap.put("dpg", "application/vnd.dpgraph");
        sMimeTypeMap.put("dfac", "application/vnd.dreamfactory");
        sMimeTypeMap.put("kpxx", "application/vnd.ds-keypoint");
        sMimeTypeMap.put("ait", "application/vnd.dvb.ait");
        sMimeTypeMap.put("svc", "application/vnd.dvb.service");
        sMimeTypeMap.put("geo", "application/vnd.dynageo");
        sMimeTypeMap.put("mag", "application/vnd.ecowin.chart");
        sMimeTypeMap.put("nml", "application/vnd.enliven");
        sMimeTypeMap.put("esf", "application/vnd.epson.esf");
        sMimeTypeMap.put("msf", "application/vnd.epson.msf");
        sMimeTypeMap.put("qam", "application/vnd.epson.quickanime");
        sMimeTypeMap.put("slt", "application/vnd.epson.salt");
        sMimeTypeMap.put("ssf", "application/vnd.epson.ssf");
        sMimeTypeMap.put("et3", "application/vnd.eszigno3+xml");
        sMimeTypeMap.put("es3", "application/vnd.eszigno3+xml");
        sMimeTypeMap.put("ez2", "application/vnd.ezpix-album");
        sMimeTypeMap.put("ez3", "application/vnd.ezpix-package");
        sMimeTypeMap.put("fdf", "application/vnd.fdf");
        sMimeTypeMap.put("mseed", "application/vnd.fdsn.mseed");
        sMimeTypeMap.put("dataless", "application/vnd.fdsn.seed");
        sMimeTypeMap.put("seed", "application/vnd.fdsn.seed");
        sMimeTypeMap.put("gph", "application/vnd.flographit");
        sMimeTypeMap.put("ftc", "application/vnd.fluxtime.clip");
        sMimeTypeMap.put("book", "application/vnd.framemaker");
        sMimeTypeMap.put("maker", "application/vnd.framemaker");
        sMimeTypeMap.put("frame", "application/vnd.framemaker");
        sMimeTypeMap.put("fm", "application/vnd.framemaker");
        sMimeTypeMap.put("fnc", "application/vnd.frogans.fnc");
        sMimeTypeMap.put("ltf", "application/vnd.frogans.ltf");
        sMimeTypeMap.put("fsc", "application/vnd.fsc.weblaunch");
        sMimeTypeMap.put("oas", "application/vnd.fujitsu.oasys");
        sMimeTypeMap.put("oa2", "application/vnd.fujitsu.oasys2");
        sMimeTypeMap.put("oa3", "application/vnd.fujitsu.oasys3");
        sMimeTypeMap.put("fg5", "application/vnd.fujitsu.oasysgp");
        sMimeTypeMap.put("bh2", "application/vnd.fujitsu.oasysprs");
        sMimeTypeMap.put("ddd", "application/vnd.fujixerox.ddd");
        sMimeTypeMap.put("xdw", "application/vnd.fujixerox.docuworks");
        sMimeTypeMap.put("xbd", "application/vnd.fujixerox.docuworks.binder");
        sMimeTypeMap.put("fzs", "application/vnd.fuzzysheet");
        sMimeTypeMap.put("txd", "application/vnd.genomatix.tuxedo");
        sMimeTypeMap.put("ggb", "application/vnd.geogebra.file");
        sMimeTypeMap.put("ggt", "application/vnd.geogebra.tool");
        sMimeTypeMap.put("gre", "application/vnd.geometry-explorer");
        sMimeTypeMap.put("gex", "application/vnd.geometry-explorer");
        sMimeTypeMap.put("gxt", "application/vnd.geonext");
        sMimeTypeMap.put("g2w", "application/vnd.geoplan");
        sMimeTypeMap.put("g3w", "application/vnd.geospace");
        sMimeTypeMap.put("gmx", "application/vnd.gmx");
        sMimeTypeMap.put("kml", "application/vnd.google-earth.kml+xml");
        sMimeTypeMap.put("kmz", "application/vnd.google-earth.kmz");
        sMimeTypeMap.put("gqs", "application/vnd.grafeq");
        sMimeTypeMap.put("gqf", "application/vnd.grafeq");
        sMimeTypeMap.put("gac", "application/vnd.groove-account");
        sMimeTypeMap.put("ghf", "application/vnd.groove-help");
        sMimeTypeMap.put("gim", "application/vnd.groove-identity-message");
        sMimeTypeMap.put("grv", "application/vnd.groove-injector");
        sMimeTypeMap.put("gtm", "application/vnd.groove-tool-message");
        sMimeTypeMap.put("tpl", "application/vnd.groove-tool-template");
        sMimeTypeMap.put("vcg", "application/vnd.groove-vcard");
        sMimeTypeMap.put("hal", "application/vnd.hal+xml");
        sMimeTypeMap.put("zmm", "application/vnd.handheld-entertainment+xml");
        sMimeTypeMap.put("hbci", "application/vnd.hbci");
        sMimeTypeMap.put("les", "application/vnd.hhe.lesson-player");
        sMimeTypeMap.put("hpgl", "application/vnd.hp-hpgl");
        sMimeTypeMap.put("hpid", "application/vnd.hp-hpid");
        sMimeTypeMap.put("hps", "application/vnd.hp-hps");
        sMimeTypeMap.put("jlt", "application/vnd.hp-jlyt");
        sMimeTypeMap.put("pcl", "application/vnd.hp-pcl");
        sMimeTypeMap.put("pclxl", "application/vnd.hp-pclxl");
        sMimeTypeMap.put("sfd-hdstx", "application/vnd.hydrostatix.sof-data");
        sMimeTypeMap.put("mpy", "application/vnd.ibm.minipay");
        sMimeTypeMap.put("list3820", "application/vnd.ibm.modcap");
        sMimeTypeMap.put("listafp", "application/vnd.ibm.modcap");
        sMimeTypeMap.put("afp", "application/vnd.ibm.modcap");
        sMimeTypeMap.put("irm", "application/vnd.ibm.rights-management");
        sMimeTypeMap.put("sc", "application/vnd.ibm.secure-container");
        sMimeTypeMap.put("icm", "application/vnd.iccprofile");
        sMimeTypeMap.put("icc", "application/vnd.iccprofile");
        sMimeTypeMap.put("igl", "application/vnd.igloader");
        sMimeTypeMap.put("ivp", "application/vnd.immervision-ivp");
        sMimeTypeMap.put("ivu", "application/vnd.immervision-ivu");
        sMimeTypeMap.put("igm", "application/vnd.insors.igm");
        sMimeTypeMap.put("xpx", "application/vnd.intercon.formnet");
        sMimeTypeMap.put("xpw", "application/vnd.intercon.formnet");
        sMimeTypeMap.put("i2g", "application/vnd.intergeo");
        sMimeTypeMap.put("qbo", "application/vnd.intu.qbo");
        sMimeTypeMap.put("qfx", "application/vnd.intu.qfx");
        sMimeTypeMap.put("rcprofile", "application/vnd.ipunplugged.rcprofile");
        sMimeTypeMap.put("irp", "application/vnd.irepository.package+xml");
        sMimeTypeMap.put("xpr", "application/vnd.is-xpr");
        sMimeTypeMap.put("fcs", "application/vnd.isac.fcs");
        sMimeTypeMap.put("jam", "application/vnd.jam");
        sMimeTypeMap.put("rms", "application/vnd.jcp.javame.midlet-rms");
        sMimeTypeMap.put("jisp", "application/vnd.jisp");
        sMimeTypeMap.put("joda", "application/vnd.joost.joda-archive");
        sMimeTypeMap.put("ktr", "application/vnd.kahootz");
        sMimeTypeMap.put("ktz", "application/vnd.kahootz");
        sMimeTypeMap.put("karbon", "application/vnd.kde.karbon");
        sMimeTypeMap.put("chrt", "application/vnd.kde.kchart");
        sMimeTypeMap.put("kfo", "application/vnd.kde.kformula");
        sMimeTypeMap.put("flw", "application/vnd.kde.kivio");
        sMimeTypeMap.put("kon", "application/vnd.kde.kontour");
        sMimeTypeMap.put("kpt", "application/vnd.kde.kpresenter");
        sMimeTypeMap.put("kpr", "application/vnd.kde.kpresenter");
        sMimeTypeMap.put("ksp", "application/vnd.kde.kspread");
        sMimeTypeMap.put("kwt", "application/vnd.kde.kword");
        sMimeTypeMap.put("kwd", "application/vnd.kde.kword");
        sMimeTypeMap.put("htke", "application/vnd.kenameaapp");
        sMimeTypeMap.put("kia", "application/vnd.kidspiration");
        sMimeTypeMap.put("knp", "application/vnd.kinar");
        sMimeTypeMap.put("kne", "application/vnd.kinar");
        sMimeTypeMap.put("skm", "application/vnd.koan");
        sMimeTypeMap.put("skt", "application/vnd.koan");
        sMimeTypeMap.put("skd", "application/vnd.koan");
        sMimeTypeMap.put("skp", "application/vnd.koan");
        sMimeTypeMap.put("sse", "application/vnd.kodak-descriptor");
        sMimeTypeMap.put("lasxml", "application/vnd.las.las+xml");
        sMimeTypeMap.put("lbd", "application/vnd.llamagraphics.life-balance.desktop");
        sMimeTypeMap.put("lbe", "application/vnd.llamagraphics.life-balance.exchange+xml");
        sMimeTypeMap.put("123", "application/vnd.lotus-1-2-3");
        sMimeTypeMap.put("apr", "application/vnd.lotus-approach");
        sMimeTypeMap.put("pre", "application/vnd.lotus-freelance");
        sMimeTypeMap.put("nsf", "application/vnd.lotus-notes");
        sMimeTypeMap.put("org", "application/vnd.lotus-organizer");
        sMimeTypeMap.put("scm", "application/vnd.lotus-screencam");
        sMimeTypeMap.put("lwp", "application/vnd.lotus-wordpro");
        sMimeTypeMap.put("portpkg", "application/vnd.macports.portpkg");
        sMimeTypeMap.put("mcd", "application/vnd.mcd");
        sMimeTypeMap.put("mc1", "application/vnd.medcalcdata");
        sMimeTypeMap.put("cdkey", "application/vnd.mediastation.cdkey");
        sMimeTypeMap.put("mwf", "application/vnd.mfer");
        sMimeTypeMap.put("mfm", "application/vnd.mfmp");
        sMimeTypeMap.put("flo", "application/vnd.micrografx.flo");
        sMimeTypeMap.put("igx", "application/vnd.micrografx.igx");
        sMimeTypeMap.put("mif", "application/vnd.mif");
        sMimeTypeMap.put("daf", "application/vnd.mobius.daf");
        sMimeTypeMap.put("dis", "application/vnd.mobius.dis");
        sMimeTypeMap.put("mbk", "application/vnd.mobius.mbk");
        sMimeTypeMap.put("mqy", "application/vnd.mobius.mqy");
        sMimeTypeMap.put("msl", "application/vnd.mobius.msl");
        sMimeTypeMap.put("plc", "application/vnd.mobius.plc");
        sMimeTypeMap.put("txf", "application/vnd.mobius.txf");
        sMimeTypeMap.put("mpn", "application/vnd.mophun.application");
        sMimeTypeMap.put("mpc", "application/vnd.mophun.certificate");
        sMimeTypeMap.put("xul", "application/vnd.mozilla.xul+xml");
        sMimeTypeMap.put("cil", "application/vnd.ms-artgalry");
        sMimeTypeMap.put("cab", "application/vnd.ms-cab-compressed");
        sMimeTypeMap.put("xlw", "application/vnd.ms-excel");
        sMimeTypeMap.put("xlt", "application/vnd.ms-excel");
        sMimeTypeMap.put("xlc", "application/vnd.ms-excel");
        sMimeTypeMap.put("xla", "application/vnd.ms-excel");
        sMimeTypeMap.put("xlm", "application/vnd.ms-excel");
        sMimeTypeMap.put("xls", "application/vnd.ms-excel");
        sMimeTypeMap.put("xlam", "application/vnd.ms-excel.addin.macroenabled.12");
        sMimeTypeMap.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroenabled.12");
        sMimeTypeMap.put("xlsm", "application/vnd.ms-excel.sheet.macroenabled.12");
        sMimeTypeMap.put("xltm", "application/vnd.ms-excel.template.macroenabled.12");
        sMimeTypeMap.put("eot", "application/vnd.ms-fontobject");
        sMimeTypeMap.put("chm", "application/vnd.ms-htmlhelp");
        sMimeTypeMap.put("ims", "application/vnd.ms-ims");
        sMimeTypeMap.put("lrm", "application/vnd.ms-lrm");
        sMimeTypeMap.put("thmx", "application/vnd.ms-officetheme");
        sMimeTypeMap.put("cat", "application/vnd.ms-pki.seccat");
        sMimeTypeMap.put("stl", "application/vnd.ms-pki.stl");
        sMimeTypeMap.put("pot", "application/vnd.ms-powerpoint");
        sMimeTypeMap.put("pps", "application/vnd.ms-powerpoint");
        sMimeTypeMap.put("ppt", "application/vnd.ms-powerpoint");
        sMimeTypeMap.put("ppam", "application/vnd.ms-powerpoint.addin.macroenabled.12");
        sMimeTypeMap.put("pptm", "application/vnd.ms-powerpoint.presentation.macroenabled.12");
        sMimeTypeMap.put("sldm", "application/vnd.ms-powerpoint.slide.macroenabled.12");
        sMimeTypeMap.put("ppsm", "application/vnd.ms-powerpoint.slideshow.macroenabled.12");
        sMimeTypeMap.put("potm", "application/vnd.ms-powerpoint.template.macroenabled.12");
        sMimeTypeMap.put("mpt", "application/vnd.ms-project");
        sMimeTypeMap.put("mpp", "application/vnd.ms-project");
        sMimeTypeMap.put("docm", "application/vnd.ms-word.document.macroenabled.12");
        sMimeTypeMap.put("dotm", "application/vnd.ms-word.template.macroenabled.12");
        sMimeTypeMap.put("wdb", "application/vnd.ms-works");
        sMimeTypeMap.put("wcm", "application/vnd.ms-works");
        sMimeTypeMap.put("wks", "application/vnd.ms-works");
        sMimeTypeMap.put("wps", "application/vnd.ms-works");
        sMimeTypeMap.put("wpl", "application/vnd.ms-wpl");
        sMimeTypeMap.put("xps", "application/vnd.ms-xpsdocument");
        sMimeTypeMap.put("mseq", "application/vnd.mseq");
        sMimeTypeMap.put("mus", "application/vnd.musician");
        sMimeTypeMap.put("msty", "application/vnd.muvee.style");
        sMimeTypeMap.put("taglet", "application/vnd.mynfc");
        sMimeTypeMap.put("nlu", "application/vnd.neurolanguage.nlu");
        sMimeTypeMap.put("nitf", "application/vnd.nitf");
        sMimeTypeMap.put("ntf", "application/vnd.nitf");
        sMimeTypeMap.put("nnd", "application/vnd.noblenet-directory");
        sMimeTypeMap.put("nns", "application/vnd.noblenet-sealer");
        sMimeTypeMap.put("nnw", "application/vnd.noblenet-web");
        sMimeTypeMap.put("ngdat", "application/vnd.nokia.n-gage.data");
        sMimeTypeMap.put("n-gage", "application/vnd.nokia.n-gage.symbian.install");
        sMimeTypeMap.put("rpst", "application/vnd.nokia.radio-preset");
        sMimeTypeMap.put("rpss", "application/vnd.nokia.radio-presets");
        sMimeTypeMap.put("edm", "application/vnd.novadigm.edm");
        sMimeTypeMap.put("edx", "application/vnd.novadigm.edx");
        sMimeTypeMap.put("ext", "application/vnd.novadigm.ext");
        sMimeTypeMap.put("odc", "application/vnd.oasis.opendocument.chart");
        sMimeTypeMap.put("otc", "application/vnd.oasis.opendocument.chart-template");
        sMimeTypeMap.put("odb", "application/vnd.oasis.opendocument.database");
        sMimeTypeMap.put("odf", "application/vnd.oasis.opendocument.formula");
        sMimeTypeMap.put("odft", "application/vnd.oasis.opendocument.formula-template");
        sMimeTypeMap.put("odg", "application/vnd.oasis.opendocument.graphics");
        sMimeTypeMap.put("otg", "application/vnd.oasis.opendocument.graphics-template");
        sMimeTypeMap.put("odi", "application/vnd.oasis.opendocument.image");
        sMimeTypeMap.put("oti", "application/vnd.oasis.opendocument.image-template");
        sMimeTypeMap.put("odp", "application/vnd.oasis.opendocument.presentation");
        sMimeTypeMap.put("otp", "application/vnd.oasis.opendocument.presentation-template");
        sMimeTypeMap.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
        sMimeTypeMap.put("ots", "application/vnd.oasis.opendocument.spreadsheet-template");
        sMimeTypeMap.put("odt", "application/vnd.oasis.opendocument.text");
        sMimeTypeMap.put("odm", "application/vnd.oasis.opendocument.text-master");
        sMimeTypeMap.put("ott", "application/vnd.oasis.opendocument.text-template");
        sMimeTypeMap.put("oth", "application/vnd.oasis.opendocument.text-web");
        sMimeTypeMap.put("xo", "application/vnd.olpc-sugar");
        sMimeTypeMap.put("dd2", "application/vnd.oma.dd2+xml");
        sMimeTypeMap.put("oxt", "application/vnd.openofficeorg.extension");
        sMimeTypeMap.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        sMimeTypeMap.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
        sMimeTypeMap.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        sMimeTypeMap.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
        sMimeTypeMap.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        sMimeTypeMap.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
        sMimeTypeMap.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        sMimeTypeMap.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        sMimeTypeMap.put("mgp", "application/vnd.osgeo.mapguide.package");
        sMimeTypeMap.put("dp", "application/vnd.osgi.dp");
        sMimeTypeMap.put("esa", "application/vnd.osgi.subsystem");
        sMimeTypeMap.put("oprc", "application/vnd.palm");
        sMimeTypeMap.put("pqa", "application/vnd.palm");
        sMimeTypeMap.put("pdb", "application/vnd.palm");
        sMimeTypeMap.put("paw", "application/vnd.pawaafile");
        sMimeTypeMap.put("str", "application/vnd.pg.format");
        sMimeTypeMap.put("ei6", "application/vnd.pg.osasli");
        sMimeTypeMap.put("efif", "application/vnd.picsel");
        sMimeTypeMap.put("wg", "application/vnd.pmi.widget");
        sMimeTypeMap.put("plf", "application/vnd.pocketlearn");
        sMimeTypeMap.put("pbd", "application/vnd.powerbuilder6");
        sMimeTypeMap.put("box", "application/vnd.previewsystems.box");
        sMimeTypeMap.put("mgz", "application/vnd.proteus.magazine");
        sMimeTypeMap.put("qps", "application/vnd.publishare-delta-tree");
        sMimeTypeMap.put("ptid", "application/vnd.pvi.ptid1");
        sMimeTypeMap.put("qxb", "application/vnd.quark.quarkxpress");
        sMimeTypeMap.put("qxl", "application/vnd.quark.quarkxpress");
        sMimeTypeMap.put("qwt", "application/vnd.quark.quarkxpress");
        sMimeTypeMap.put("qwd", "application/vnd.quark.quarkxpress");
        sMimeTypeMap.put("qxt", "application/vnd.quark.quarkxpress");
        sMimeTypeMap.put("qxd", "application/vnd.quark.quarkxpress");
        sMimeTypeMap.put("bed", "application/vnd.realvnc.bed");
        sMimeTypeMap.put("mxl", "application/vnd.recordare.musicxml");
        sMimeTypeMap.put("musicxml", "application/vnd.recordare.musicxml+xml");
        sMimeTypeMap.put("cryptonote", "application/vnd.rig.cryptonote");
        sMimeTypeMap.put("cod", "application/vnd.rim.cod");
        sMimeTypeMap.put("rm", "application/vnd.rn-realmedia");
        sMimeTypeMap.put("rmvb", "application/vnd.rn-realmedia-vbr");
        sMimeTypeMap.put("link66", "application/vnd.route66.link66+xml");
        sMimeTypeMap.put("st", "application/vnd.sailingtracker.track");
        sMimeTypeMap.put("see", "application/vnd.seemail");
        sMimeTypeMap.put("sema", "application/vnd.sema");
        sMimeTypeMap.put("semd", "application/vnd.semd");
        sMimeTypeMap.put("semf", "application/vnd.semf");
        sMimeTypeMap.put("ifm", "application/vnd.shana.informed.formdata");
        sMimeTypeMap.put("itp", "application/vnd.shana.informed.formtemplate");
        sMimeTypeMap.put("iif", "application/vnd.shana.informed.interchange");
        sMimeTypeMap.put("ipk", "application/vnd.shana.informed.package");
        sMimeTypeMap.put("twds", "application/vnd.simtech-mindmapper");
        sMimeTypeMap.put("twd", "application/vnd.simtech-mindmapper");
        sMimeTypeMap.put("mmf", "application/vnd.smaf");
        sMimeTypeMap.put("teacher", "application/vnd.smart.teacher");
        sMimeTypeMap.put("sdkd", "application/vnd.solent.sdkm+xml");
        sMimeTypeMap.put("sdkm", "application/vnd.solent.sdkm+xml");
        sMimeTypeMap.put("dxp", "application/vnd.spotfire.dxp");
        sMimeTypeMap.put("sfs", "application/vnd.spotfire.sfs");
        sMimeTypeMap.put("sdc", "application/vnd.stardivision.calc");
        sMimeTypeMap.put("sda", "application/vnd.stardivision.draw");
        sMimeTypeMap.put("sdd", "application/vnd.stardivision.impress");
        sMimeTypeMap.put("smf", "application/vnd.stardivision.math");
        sMimeTypeMap.put("vor", "application/vnd.stardivision.writer");
        sMimeTypeMap.put("sdw", "application/vnd.stardivision.writer");
        sMimeTypeMap.put("sgl", "application/vnd.stardivision.writer-global");
        sMimeTypeMap.put("smzip", "application/vnd.stepmania.package");
        sMimeTypeMap.put("sm", "application/vnd.stepmania.stepchart");
        sMimeTypeMap.put("sxc", "application/vnd.sun.xml.calc");
        sMimeTypeMap.put("stc", "application/vnd.sun.xml.calc.template");
        sMimeTypeMap.put("sxd", "application/vnd.sun.xml.draw");
        sMimeTypeMap.put("std", "application/vnd.sun.xml.draw.template");
        sMimeTypeMap.put("sxi", "application/vnd.sun.xml.impress");
        sMimeTypeMap.put("sti", "application/vnd.sun.xml.impress.template");
        sMimeTypeMap.put("sxm", "application/vnd.sun.xml.math");
        sMimeTypeMap.put("sxw", "application/vnd.sun.xml.writer");
        sMimeTypeMap.put("sxg", "application/vnd.sun.xml.writer.global");
        sMimeTypeMap.put("stw", "application/vnd.sun.xml.writer.template");
        sMimeTypeMap.put("susp", "application/vnd.sus-calendar");
        sMimeTypeMap.put("sus", "application/vnd.sus-calendar");
        sMimeTypeMap.put("svd", "application/vnd.svd");
        sMimeTypeMap.put("sisx", "application/vnd.symbian.install");
        sMimeTypeMap.put("sis", "application/vnd.symbian.install");
        sMimeTypeMap.put("xsm", "application/vnd.syncml+xml");
        sMimeTypeMap.put("bdm", "application/vnd.syncml.dm+wbxml");
        sMimeTypeMap.put("xdm", "application/vnd.syncml.dm+xml");
        sMimeTypeMap.put("tao", "application/vnd.tao.intent-module-archive");
        sMimeTypeMap.put("dmp", "application/vnd.tcpdump.pcap");
        sMimeTypeMap.put("cap", "application/vnd.tcpdump.pcap");
        sMimeTypeMap.put("pcap", "application/vnd.tcpdump.pcap");
        sMimeTypeMap.put("tmo", "application/vnd.tmobile-livetv");
        sMimeTypeMap.put("tpt", "application/vnd.trid.tpt");
        sMimeTypeMap.put("mxs", "application/vnd.triscape.mxs");
        sMimeTypeMap.put("tra", "application/vnd.trueapp");
        sMimeTypeMap.put("ufdl", "application/vnd.ufdl");
        sMimeTypeMap.put("ufd", "application/vnd.ufdl");
        sMimeTypeMap.put("utz", "application/vnd.uiq.theme");
        sMimeTypeMap.put("umj", "application/vnd.umajin");
        sMimeTypeMap.put("unityweb", "application/vnd.unity");
        sMimeTypeMap.put("uoml", "application/vnd.uoml+xml");
        sMimeTypeMap.put("vcx", "application/vnd.vcx");
        sMimeTypeMap.put("vsw", "application/vnd.visio");
        sMimeTypeMap.put("vss", "application/vnd.visio");
        sMimeTypeMap.put("vst", "application/vnd.visio");
        sMimeTypeMap.put("vsd", "application/vnd.visio");
        sMimeTypeMap.put("vis", "application/vnd.visionary");
        sMimeTypeMap.put("vsf", "application/vnd.vsf");
        sMimeTypeMap.put("wbxml", "application/vnd.wap.wbxml");
        sMimeTypeMap.put("wmlc", "application/vnd.wap.wmlc");
        sMimeTypeMap.put("wmlsc", "application/vnd.wap.wmlscriptc");
        sMimeTypeMap.put("wtb", "application/vnd.webturbo");
        sMimeTypeMap.put("nbp", "application/vnd.wolfram.player");
        sMimeTypeMap.put("wpd", "application/vnd.wordperfect");
        sMimeTypeMap.put("wqd", "application/vnd.wqd");
        sMimeTypeMap.put("stf", "application/vnd.wt.stf");
        sMimeTypeMap.put("xar", "application/vnd.xara");
        sMimeTypeMap.put("xfdl", "application/vnd.xfdl");
        sMimeTypeMap.put("hvd", "application/vnd.yamaha.hv-dic");
        sMimeTypeMap.put("hvs", "application/vnd.yamaha.hv-script");
        sMimeTypeMap.put("hvp", "application/vnd.yamaha.hv-voice");
        sMimeTypeMap.put("osf", "application/vnd.yamaha.openscoreformat");
        sMimeTypeMap.put("osfpvg", "application/vnd.yamaha.openscoreformat.osfpvg+xml");
        sMimeTypeMap.put("saf", "application/vnd.yamaha.smaf-audio");
        sMimeTypeMap.put("spf", "application/vnd.yamaha.smaf-phrase");
        sMimeTypeMap.put("cmp", "application/vnd.yellowriver-custom-menu");
        sMimeTypeMap.put("zirz", "application/vnd.zul");
        sMimeTypeMap.put("zir", "application/vnd.zul");
        sMimeTypeMap.put("zaz", "application/vnd.zzazz.deck+xml");
        sMimeTypeMap.put("vxml", "application/voicexml+xml");
        sMimeTypeMap.put("wgt", "application/widget");
        sMimeTypeMap.put("hlp", "application/winhlp");
        sMimeTypeMap.put("wsdl", "application/wsdl+xml");
        sMimeTypeMap.put("wspolicy", "application/wspolicy+xml");
        sMimeTypeMap.put("7z", "application/x-7z-compressed");
        sMimeTypeMap.put("abw", "application/x-abiword");
        sMimeTypeMap.put("ace", "application/x-ace-compressed");
        sMimeTypeMap.put("dmg", "application/x-apple-diskimage");
        sMimeTypeMap.put("vox", "application/x-authorware-bin");
        sMimeTypeMap.put("u32", "application/x-authorware-bin");
        sMimeTypeMap.put("x32", "application/x-authorware-bin");
        sMimeTypeMap.put("aab", "application/x-authorware-bin");
        sMimeTypeMap.put("aam", "application/x-authorware-map");
        sMimeTypeMap.put("aas", "application/x-authorware-seg");
        sMimeTypeMap.put("bcpio", "application/x-bcpio");
        sMimeTypeMap.put("torrent", "application/x-bittorrent");
        sMimeTypeMap.put("blorb", "application/x-blorb");
        sMimeTypeMap.put("blb", "application/x-blorb");
        sMimeTypeMap.put("bz", "application/x-bzip");
        sMimeTypeMap.put("boz", "application/x-bzip2");
        sMimeTypeMap.put("bz2", "application/x-bzip2");
        sMimeTypeMap.put("cb7", "application/x-cbr");
        sMimeTypeMap.put("cbz", "application/x-cbr");
        sMimeTypeMap.put("cbt", "application/x-cbr");
        sMimeTypeMap.put("cba", "application/x-cbr");
        sMimeTypeMap.put("cbr", "application/x-cbr");
        sMimeTypeMap.put("vcd", "application/x-cdlink");
        sMimeTypeMap.put("cfs", "application/x-cfs-compressed");
        sMimeTypeMap.put("chat", "application/x-chat");
        sMimeTypeMap.put("pgn", "application/x-chess-pgn");
        sMimeTypeMap.put("nsc", "application/x-conference");
        sMimeTypeMap.put("cpio", "application/x-cpio");
        sMimeTypeMap.put("csh", "application/x-csh");
        sMimeTypeMap.put("udeb", "application/x-debian-package");
        sMimeTypeMap.put("deb", "application/x-debian-package");
        sMimeTypeMap.put("dgc", "application/x-dgc-compressed");
        sMimeTypeMap.put("swa", "application/x-director");
        sMimeTypeMap.put("fgd", "application/x-director");
        sMimeTypeMap.put("w3d", "application/x-director");
        sMimeTypeMap.put("cxt", "application/x-director");
        sMimeTypeMap.put("cct", "application/x-director");
        sMimeTypeMap.put("cst", "application/x-director");
        sMimeTypeMap.put("dxr", "application/x-director");
        sMimeTypeMap.put("dcr", "application/x-director");
        sMimeTypeMap.put("dir", "application/x-director");
        sMimeTypeMap.put("wad", "application/x-doom");
        sMimeTypeMap.put("ncx", "application/x-dtbncx+xml");
        sMimeTypeMap.put("dtb", "application/x-dtbook+xml");
        sMimeTypeMap.put("res", "application/x-dtbresource+xml");
        sMimeTypeMap.put("dvi", "application/x-dvi");
        sMimeTypeMap.put("evy", "application/x-envoy");
        sMimeTypeMap.put("eva", "application/x-eva");
        sMimeTypeMap.put("bdf", "application/x-font-bdf");
        sMimeTypeMap.put("gsf", "application/x-font-ghostscript");
        sMimeTypeMap.put("psf", "application/x-font-linux-psf");
        sMimeTypeMap.put("otf", "application/x-font-otf");
        sMimeTypeMap.put("pcf", "application/x-font-pcf");
        sMimeTypeMap.put("snf", "application/x-font-snf");
        sMimeTypeMap.put("ttc", "application/x-font-ttf");
        sMimeTypeMap.put("ttf", "application/x-font-ttf");
        sMimeTypeMap.put("afm", "application/x-font-type1");
        sMimeTypeMap.put("pfm", "application/x-font-type1");
        sMimeTypeMap.put("pfb", "application/x-font-type1");
        sMimeTypeMap.put("pfa", "application/x-font-type1");
        sMimeTypeMap.put("woff", "application/font-woff");
        sMimeTypeMap.put("arc", "application/x-freearc");
        sMimeTypeMap.put("spl", "application/x-futuresplash");
        sMimeTypeMap.put("gca", "application/x-gca-compressed");
        sMimeTypeMap.put("ulx", "application/x-glulx");
        sMimeTypeMap.put("gnumeric", "application/x-gnumeric");
        sMimeTypeMap.put("gramps", "application/x-gramps-xml");
        sMimeTypeMap.put("gtar", "application/x-gtar");
        sMimeTypeMap.put("hdf", "application/x-hdf");
        sMimeTypeMap.put("install", "application/x-install-instructions");
        sMimeTypeMap.put("iso", "application/x-iso9660-image");
        sMimeTypeMap.put("jnlp", "application/x-java-jnlp-file");
        sMimeTypeMap.put("latex", "application/x-latex");
        sMimeTypeMap.put("lha", "application/x-lzh-compressed");
        sMimeTypeMap.put("lzh", "application/x-lzh-compressed");
        sMimeTypeMap.put("mie", "application/x-mie");
        sMimeTypeMap.put("mobi", "application/x-mobipocket-ebook");
        sMimeTypeMap.put("prc", "application/x-mobipocket-ebook");
        sMimeTypeMap.put("application", "application/x-ms-application");
        sMimeTypeMap.put("lnk", "application/x-ms-shortcut");
        sMimeTypeMap.put("wmd", "application/x-ms-wmd");
        sMimeTypeMap.put("wmz", "application/x-ms-wmz");
        sMimeTypeMap.put("xbap", "application/x-ms-xbap");
        sMimeTypeMap.put("mdb", "application/x-msaccess");
        sMimeTypeMap.put("obd", "application/x-msbinder");
        sMimeTypeMap.put("crd", "application/x-mscardfile");
        sMimeTypeMap.put("clp", "application/x-msclip");
        sMimeTypeMap.put("msi", "application/x-msdownload");
        sMimeTypeMap.put("bat", "application/x-msdownload");
        sMimeTypeMap.put("com", "application/x-msdownload");
        sMimeTypeMap.put("dll", "application/x-msdownload");
        sMimeTypeMap.put("exe", "application/x-msdownload");
        sMimeTypeMap.put("m14", "application/x-msmediaview");
        sMimeTypeMap.put("m13", "application/x-msmediaview");
        sMimeTypeMap.put("mvb", "application/x-msmediaview");
        sMimeTypeMap.put("emz", "application/x-msmetafile");
        sMimeTypeMap.put("emf", "application/x-msmetafile");
        sMimeTypeMap.put("wmz", "application/x-msmetafile");
        sMimeTypeMap.put("wmf", "application/x-msmetafile");
        sMimeTypeMap.put("mny", "application/x-msmoney");
        sMimeTypeMap.put("pub", "application/x-mspublisher");
        sMimeTypeMap.put("scd", "application/x-msschedule");
        sMimeTypeMap.put("trm", "application/x-msterminal");
        sMimeTypeMap.put("wri", "application/x-mswrite");
        sMimeTypeMap.put("cdf", "application/x-netcdf");
        sMimeTypeMap.put("nc", "application/x-netcdf");
        sMimeTypeMap.put("nzb", "application/x-nzb");
        sMimeTypeMap.put("pfx", "application/x-pkcs12");
        sMimeTypeMap.put("p12", "application/x-pkcs12");
        sMimeTypeMap.put("spc", "application/x-pkcs7-certificates");
        sMimeTypeMap.put("p7b", "application/x-pkcs7-certificates");
        sMimeTypeMap.put("p7r", "application/x-pkcs7-certreqresp");
        sMimeTypeMap.put("rar", "application/x-rar-compressed");
        sMimeTypeMap.put("ris", "application/x-research-info-systems");
        sMimeTypeMap.put("sh", "application/x-sh");
        sMimeTypeMap.put("shar", "application/x-shar");
        sMimeTypeMap.put("swf", "application/x-shockwave-flash");
        sMimeTypeMap.put("xap", "application/x-silverlight-app");
        sMimeTypeMap.put("sql", "application/x-sql");
        sMimeTypeMap.put("sit", "application/x-stuffit");
        sMimeTypeMap.put("sitx", "application/x-stuffitx");
        sMimeTypeMap.put("srt", "application/x-subrip");
        sMimeTypeMap.put("sv4cpio", "application/x-sv4cpio");
        sMimeTypeMap.put("sv4crc", "application/x-sv4crc");
        sMimeTypeMap.put("t3", "application/x-t3vm-image");
        sMimeTypeMap.put("gam", "application/x-tads");
        sMimeTypeMap.put("tar", "application/x-tar");
        sMimeTypeMap.put("tcl", "application/x-tcl");
        sMimeTypeMap.put("tex", "application/x-tex");
        sMimeTypeMap.put("tfm", "application/x-tex-tfm");
        sMimeTypeMap.put("texi", "application/x-texinfo");
        sMimeTypeMap.put("texinfo", "application/x-texinfo");
        sMimeTypeMap.put("obj", "application/x-tgif");
        sMimeTypeMap.put("ustar", "application/x-ustar");
        sMimeTypeMap.put("src", "application/x-wais-source");
        sMimeTypeMap.put("crt", "application/x-x509-ca-cert");
        sMimeTypeMap.put("der", "application/x-x509-ca-cert");
        sMimeTypeMap.put("fig", "application/x-xfig");
        sMimeTypeMap.put("xlf", "application/x-xliff+xml");
        sMimeTypeMap.put("xpi", "application/x-xpinstall");
        sMimeTypeMap.put("xz", "application/x-xz");
        sMimeTypeMap.put("z8", "application/x-zmachine");
        sMimeTypeMap.put("z7", "application/x-zmachine");
        sMimeTypeMap.put("z6", "application/x-zmachine");
        sMimeTypeMap.put("z5", "application/x-zmachine");
        sMimeTypeMap.put("z4", "application/x-zmachine");
        sMimeTypeMap.put("z3", "application/x-zmachine");
        sMimeTypeMap.put("z2", "application/x-zmachine");
        sMimeTypeMap.put("z1", "application/x-zmachine");
        sMimeTypeMap.put("xaml", "application/xaml+xml");
        sMimeTypeMap.put("xdf", "application/xcap-diff+xml");
        sMimeTypeMap.put("xenc", "application/xenc+xml");
        sMimeTypeMap.put("xht", "application/xhtml+xml");
        sMimeTypeMap.put("xhtml", "application/xhtml+xml");
        sMimeTypeMap.put("xsl", "application/xml");
        sMimeTypeMap.put("xml", "application/xml");
        sMimeTypeMap.put("dtd", "application/xml-dtd");
        sMimeTypeMap.put("xop", "application/xop+xml");
        sMimeTypeMap.put("xpl", "application/xproc+xml");
        sMimeTypeMap.put("xslt", "application/xslt+xml");
        sMimeTypeMap.put("xspf", "application/xspf+xml");
        sMimeTypeMap.put("xvm", "application/xv+xml");
        sMimeTypeMap.put("xvml", "application/xv+xml");
        sMimeTypeMap.put("xhvml", "application/xv+xml");
        sMimeTypeMap.put("mxml", "application/xv+xml");
        sMimeTypeMap.put("yang", "application/yang");
        sMimeTypeMap.put("yin", "application/yin+xml");
        sMimeTypeMap.put("zip", "application/zip");
        sMimeTypeMap.put("adp", "audio/adpcm");
        sMimeTypeMap.put("snd", "audio/basic");
        sMimeTypeMap.put("au", "audio/basic");
        sMimeTypeMap.put("rmi", "audio/midi");
        sMimeTypeMap.put("kar", "audio/midi");
        sMimeTypeMap.put("midi", "audio/midi");
        sMimeTypeMap.put("mid", "audio/midi");
        sMimeTypeMap.put("mp4a", "audio/mp4");
        sMimeTypeMap.put("m3a", "audio/mpeg");
        sMimeTypeMap.put("m2a", "audio/mpeg");
        sMimeTypeMap.put("mp3", "audio/mpeg");
        sMimeTypeMap.put("mp2a", "audio/mpeg");
        sMimeTypeMap.put("mp2", "audio/mpeg");
        sMimeTypeMap.put("mpga", "audio/mpeg");
        sMimeTypeMap.put("spx", "audio/ogg");
        sMimeTypeMap.put("ogg", "audio/ogg");
        sMimeTypeMap.put("oga", "audio/ogg");
        sMimeTypeMap.put("s3m", "audio/s3m");
        sMimeTypeMap.put("sil", "audio/silk");
        sMimeTypeMap.put("uvva", "audio/vnd.dece.audio");
        sMimeTypeMap.put("uva", "audio/vnd.dece.audio");
        sMimeTypeMap.put("eol", "audio/vnd.digital-winds");
        sMimeTypeMap.put("dra", "audio/vnd.dra");
        sMimeTypeMap.put("dts", "audio/vnd.dts");
        sMimeTypeMap.put("dtshd", "audio/vnd.dts.hd");
        sMimeTypeMap.put("lvp", "audio/vnd.lucent.voice");
        sMimeTypeMap.put("pya", "audio/vnd.ms-playready.media.pya");
        sMimeTypeMap.put("ecelp4800", "audio/vnd.nuera.ecelp4800");
        sMimeTypeMap.put("ecelp7470", "audio/vnd.nuera.ecelp7470");
        sMimeTypeMap.put("ecelp9600", "audio/vnd.nuera.ecelp9600");
        sMimeTypeMap.put("rip", "audio/vnd.rip");
        sMimeTypeMap.put("weba", "audio/webm");
        sMimeTypeMap.put("aac", "audio/x-aac");
        sMimeTypeMap.put("aifc", "audio/x-aiff");
        sMimeTypeMap.put("aiff", "audio/x-aiff");
        sMimeTypeMap.put("aif", "audio/x-aiff");
        sMimeTypeMap.put("caf", "audio/x-caf");
        sMimeTypeMap.put("flac", "audio/x-flac");
        sMimeTypeMap.put("mka", "audio/x-matroska");
        sMimeTypeMap.put("m3u", "audio/x-mpegurl");
        sMimeTypeMap.put("wax", "audio/x-ms-wax");
        sMimeTypeMap.put("wma", "audio/x-ms-wma");
        sMimeTypeMap.put("ra", "audio/x-pn-realaudio");
        sMimeTypeMap.put("ram", "audio/x-pn-realaudio");
        sMimeTypeMap.put("rmp", "audio/x-pn-realaudio-plugin");
        sMimeTypeMap.put("wav", "audio/x-wav");
        sMimeTypeMap.put("xm", "audio/xm");
        sMimeTypeMap.put("cdx", "chemical/x-cdx");
        sMimeTypeMap.put("cif", "chemical/x-cif");
        sMimeTypeMap.put("cmdf", "chemical/x-cmdf");
        sMimeTypeMap.put("cml", "chemical/x-cml");
        sMimeTypeMap.put("csml", "chemical/x-csml");
        sMimeTypeMap.put("xyz", "chemical/x-xyz");
        sMimeTypeMap.put("bmp", "image/bmp");
        sMimeTypeMap.put("cgm", "image/cgm");
        sMimeTypeMap.put("g3", "image/g3fax");
        sMimeTypeMap.put("gif", "image/gif");
        sMimeTypeMap.put("ief", "image/ief");
        sMimeTypeMap.put("jpe", "image/jpeg");
        sMimeTypeMap.put("jpg", "image/jpeg");
        sMimeTypeMap.put("jpeg", "image/jpeg");
        sMimeTypeMap.put("ktx", "image/ktx");
        sMimeTypeMap.put("png", "image/png");
        sMimeTypeMap.put("btif", "image/prs.btif");
        sMimeTypeMap.put("sgi", "image/sgi");
        sMimeTypeMap.put("svgz", "image/svg+xml");
        sMimeTypeMap.put("svg", "image/svg+xml");
        sMimeTypeMap.put("tif", "image/tiff");
        sMimeTypeMap.put("tiff", "image/tiff");
        sMimeTypeMap.put("psd", "image/vnd.adobe.photoshop");
        sMimeTypeMap.put("uvvg", "image/vnd.dece.graphic");
        sMimeTypeMap.put("uvg", "image/vnd.dece.graphic");
        sMimeTypeMap.put("uvvi", "image/vnd.dece.graphic");
        sMimeTypeMap.put("uvi", "image/vnd.dece.graphic");
        sMimeTypeMap.put("sub", "image/vnd.dvb.subtitle");
        sMimeTypeMap.put("djv", "image/vnd.djvu");
        sMimeTypeMap.put("djvu", "image/vnd.djvu");
        sMimeTypeMap.put("dwg", "image/vnd.dwg");
        sMimeTypeMap.put("dxf", "image/vnd.dxf");
        sMimeTypeMap.put("fbs", "image/vnd.fastbidsheet");
        sMimeTypeMap.put("fpx", "image/vnd.fpx");
        sMimeTypeMap.put("fst", "image/vnd.fst");
        sMimeTypeMap.put("mmr", "image/vnd.fujixerox.edmics-mmr");
        sMimeTypeMap.put("rlc", "image/vnd.fujixerox.edmics-rlc");
        sMimeTypeMap.put("mdi", "image/vnd.ms-modi");
        sMimeTypeMap.put("wdp", "image/vnd.ms-photo");
        sMimeTypeMap.put("npx", "image/vnd.net-fpx");
        sMimeTypeMap.put("wbmp", "image/vnd.wap.wbmp");
        sMimeTypeMap.put("xif", "image/vnd.xiff");
        sMimeTypeMap.put("webp", "image/webp");
        sMimeTypeMap.put("3ds", "image/x-3ds");
        sMimeTypeMap.put("ras", "image/x-cmu-raster");
        sMimeTypeMap.put("cmx", "image/x-cmx");
        sMimeTypeMap.put("fh7", "image/x-freehand");
        sMimeTypeMap.put("fh5", "image/x-freehand");
        sMimeTypeMap.put("fh4", "image/x-freehand");
        sMimeTypeMap.put("fhc", "image/x-freehand");
        sMimeTypeMap.put("fh", "image/x-freehand");
        sMimeTypeMap.put("ico", "image/x-icon");
        sMimeTypeMap.put("sid", "image/x-mrsid-image");
        sMimeTypeMap.put("pcx", "image/x-pcx");
        sMimeTypeMap.put("pct", "image/x-pict");
        sMimeTypeMap.put("pic", "image/x-pict");
        sMimeTypeMap.put("pnm", "image/x-portable-anymap");
        sMimeTypeMap.put("pbm", "image/x-portable-bitmap");
        sMimeTypeMap.put("pgm", "image/x-portable-graymap");
        sMimeTypeMap.put("ppm", "image/x-portable-pixmap");
        sMimeTypeMap.put("rgb", "image/x-rgb");
        sMimeTypeMap.put("tga", "image/x-tga");
        sMimeTypeMap.put("xbm", "image/x-xbitmap");
        sMimeTypeMap.put("xpm", "image/x-xpixmap");
        sMimeTypeMap.put("xwd", "image/x-xwindowdump");
        sMimeTypeMap.put("mime", "message/rfc822");
        sMimeTypeMap.put("eml", "message/rfc822");
        sMimeTypeMap.put("iges", "model/iges");
        sMimeTypeMap.put("igs", "model/iges");
        sMimeTypeMap.put("silo", "model/mesh");
        sMimeTypeMap.put("mesh", "model/mesh");
        sMimeTypeMap.put("msh", "model/mesh");
        sMimeTypeMap.put("dae", "model/vnd.collada+xml");
        sMimeTypeMap.put("dwf", "model/vnd.dwf");
        sMimeTypeMap.put("gdl", "model/vnd.gdl");
        sMimeTypeMap.put("gtw", "model/vnd.gtw");
        sMimeTypeMap.put("mts", "model/vnd.mts");
        sMimeTypeMap.put("vtu", "model/vnd.vtu");
        sMimeTypeMap.put("vrml", "model/vrml");
        sMimeTypeMap.put("wrl", "model/vrml");
        sMimeTypeMap.put("x3dbz", "model/x3d+binary");
        sMimeTypeMap.put("x3db", "model/x3d+binary");
        sMimeTypeMap.put("x3dvz", "model/x3d+vrml");
        sMimeTypeMap.put("x3dv", "model/x3d+vrml");
        sMimeTypeMap.put("x3dz", "model/x3d+xml");
        sMimeTypeMap.put("x3d", "model/x3d+xml");
        sMimeTypeMap.put("appcache", "text/cache-manifest");
        sMimeTypeMap.put("ifb", "text/calendar");
        sMimeTypeMap.put("ics", "text/calendar");
        sMimeTypeMap.put("css", "text/css");
        sMimeTypeMap.put("csv", "text/csv");
        sMimeTypeMap.put("htm", "text/html");
        sMimeTypeMap.put("html", "text/html");
        sMimeTypeMap.put("n3", "text/n3");
        sMimeTypeMap.put("in", "text/plain");
        sMimeTypeMap.put("log", "text/plain");
        sMimeTypeMap.put("list", "text/plain");
        sMimeTypeMap.put("def", "text/plain");
        sMimeTypeMap.put("conf", "text/plain");
        sMimeTypeMap.put("text", "text/plain");
        sMimeTypeMap.put("txt", "text/plain");
        sMimeTypeMap.put("dsc", "text/prs.lines.tag");
        sMimeTypeMap.put("rtx", "text/richtext");
        sMimeTypeMap.put("sgm", "text/sgml");
        sMimeTypeMap.put("sgml", "text/sgml");
        sMimeTypeMap.put("tsv", "text/tab-separated-values");
        sMimeTypeMap.put("ms", "text/troff");
        sMimeTypeMap.put("me", "text/troff");
        sMimeTypeMap.put("man", "text/troff");
        sMimeTypeMap.put("roff", "text/troff");
        sMimeTypeMap.put("tr", "text/troff");
        sMimeTypeMap.put("t", "text/troff");
        sMimeTypeMap.put("ttl", "text/turtle");
        sMimeTypeMap.put("urls", "text/uri-list");
        sMimeTypeMap.put("uris", "text/uri-list");
        sMimeTypeMap.put("uri", "text/uri-list");
        sMimeTypeMap.put("vcard", "text/vcard");
        sMimeTypeMap.put("curl", "text/vnd.curl");
        sMimeTypeMap.put("dcurl", "text/vnd.curl.dcurl");
        sMimeTypeMap.put("scurl", "text/vnd.curl.scurl");
        sMimeTypeMap.put("mcurl", "text/vnd.curl.mcurl");
        sMimeTypeMap.put("sub", "text/vnd.dvb.subtitle");
        sMimeTypeMap.put("fly", "text/vnd.fly");
        sMimeTypeMap.put("flx", "text/vnd.fmi.flexstor");
        sMimeTypeMap.put("gv", "text/vnd.graphviz");
        sMimeTypeMap.put("3dml", "text/vnd.in3d.3dml");
        sMimeTypeMap.put("spot", "text/vnd.in3d.spot");
        sMimeTypeMap.put("jad", "text/vnd.sun.j2me.app-descriptor");
        sMimeTypeMap.put("wml", "text/vnd.wap.wml");
        sMimeTypeMap.put("wmls", "text/vnd.wap.wmlscript");
        sMimeTypeMap.put("asm", "text/x-asm");
        sMimeTypeMap.put("s", "text/x-asm");
        sMimeTypeMap.put("dic", "text/x-c");
        sMimeTypeMap.put("hh", "text/x-c");
        sMimeTypeMap.put("h", "text/x-c");
        sMimeTypeMap.put("cpp", "text/x-c");
        sMimeTypeMap.put("cxx", "text/x-c");
        sMimeTypeMap.put("cc", "text/x-c");
        sMimeTypeMap.put("c", "text/x-c");
        sMimeTypeMap.put("f90", "text/x-fortran");
        sMimeTypeMap.put("f77", "text/x-fortran");
        sMimeTypeMap.put("for", "text/x-fortran");
        sMimeTypeMap.put("f", "text/x-fortran");
        sMimeTypeMap.put("java", "text/x-java-source");
        sMimeTypeMap.put("opml", "text/x-opml");
        sMimeTypeMap.put("pas", "text/x-pascal");
        sMimeTypeMap.put("p", "text/x-pascal");
        sMimeTypeMap.put("nfo", "text/x-nfo");
        sMimeTypeMap.put("etx", "text/x-setext");
        sMimeTypeMap.put("sfv", "text/x-sfv");
        sMimeTypeMap.put("uu", "text/x-uuencode");
        sMimeTypeMap.put("vcs", "text/x-vcalendar");
        sMimeTypeMap.put("vcf", "text/x-vcard");
        sMimeTypeMap.put("3gp", "video/3gpp");
        sMimeTypeMap.put("3g2", "video/3gpp2");
        sMimeTypeMap.put("h261", "video/h261");
        sMimeTypeMap.put("h263", "video/h263");
        sMimeTypeMap.put("h264", "video/h264");
        sMimeTypeMap.put("jpgv", "video/jpeg");
        sMimeTypeMap.put("jpgm", "video/jpm");
        sMimeTypeMap.put("jpm", "video/jpm");
        sMimeTypeMap.put("mjp2", "video/mj2");
        sMimeTypeMap.put("mj2", "video/mj2");
        sMimeTypeMap.put("mpg4", "video/mp4");
        sMimeTypeMap.put("mp4v", "video/mp4");
        sMimeTypeMap.put("mp4", "video/mp4");
        sMimeTypeMap.put("m2v", "video/mpeg");
        sMimeTypeMap.put("m1v", "video/mpeg");
        sMimeTypeMap.put("mpe", "video/mpeg");
        sMimeTypeMap.put("mpg", "video/mpeg");
        sMimeTypeMap.put("mpeg", "video/mpeg");
        sMimeTypeMap.put("ogv", "video/ogg");
        sMimeTypeMap.put("mov", "video/quicktime");
        sMimeTypeMap.put("qt", "video/quicktime");
        sMimeTypeMap.put("uvvh", "video/vnd.dece.hd");
        sMimeTypeMap.put("uvh", "video/vnd.dece.hd");
        sMimeTypeMap.put("uvvm", "video/vnd.dece.mobile");
        sMimeTypeMap.put("uvm", "video/vnd.dece.mobile");
        sMimeTypeMap.put("uvvp", "video/vnd.dece.pd");
        sMimeTypeMap.put("uvp", "video/vnd.dece.pd");
        sMimeTypeMap.put("uvvs", "video/vnd.dece.sd");
        sMimeTypeMap.put("uvs", "video/vnd.dece.sd");
        sMimeTypeMap.put("uvvv", "video/vnd.dece.video");
        sMimeTypeMap.put("uvv", "video/vnd.dece.video");
        sMimeTypeMap.put("dvb", "video/vnd.dvb.file");
        sMimeTypeMap.put("fvt", "video/vnd.fvt");
        sMimeTypeMap.put("m4u", "video/vnd.mpegurl");
        sMimeTypeMap.put("mxu", "video/vnd.mpegurl");
        sMimeTypeMap.put("pyv", "video/vnd.ms-playready.media.pyv");
        sMimeTypeMap.put("uvvu", "video/vnd.uvvu.mp4");
        sMimeTypeMap.put("uvu", "video/vnd.uvvu.mp4");
        sMimeTypeMap.put("viv", "video/vnd.vivo");
        sMimeTypeMap.put("webm", "video/webm");
        sMimeTypeMap.put("f4v", "video/x-f4v");
        sMimeTypeMap.put("fli", "video/x-fli");
        sMimeTypeMap.put("flv", "video/x-flv");
        sMimeTypeMap.put("m4v", "video/x-m4v");
        sMimeTypeMap.put("mks", "video/x-matroska");
        sMimeTypeMap.put("mk3d", "video/x-matroska");
        sMimeTypeMap.put("mkv", "video/x-matroska");
        sMimeTypeMap.put("mng", "video/x-mng");
        sMimeTypeMap.put("asx", "video/x-ms-asf");
        sMimeTypeMap.put("asf", "video/x-ms-asf");
        sMimeTypeMap.put("vob", "video/x-ms-vob");
        sMimeTypeMap.put("wm", "video/x-ms-wm");
        sMimeTypeMap.put("wmv", "video/x-ms-wmv");
        sMimeTypeMap.put("wmx", "video/x-ms-wmx");
        sMimeTypeMap.put("wvx", "video/x-ms-wvx");
        sMimeTypeMap.put("avi", "video/x-msvideo");
        sMimeTypeMap.put("movie", "video/x-sgi-movie");
        sMimeTypeMap.put("smv", "video/x-smv");
        sMimeTypeMap.put("ice", "x-conference/x-cooltalk");
    }

    /**
     * Returns the file extension or an empty string iff there is no
     * extension. This method is a convenience method for obtaining the
     * extension of a url and has undefined results for other Strings.
     *
     * @param url
     * @return The file extension of the given url.
     */
    public static String getFileExtensionFromUrl(String url) {
        if (url != null && url.length() > 0) {
            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }
            int filenamePos = url.lastIndexOf('/');
            String filename =
                    0 <= filenamePos ? url.substring(filenamePos + 1) : url;

            // if the filename contains special characters, we don't
            // consider it valid for our matching purposes:
            if (filename.length() > 0 &&
                    Pattern.matches("[a-zA-Z_0-9\\.\\-\\(\\)\\%]+", filename)) {
                int dotPos = filename.lastIndexOf('.');
                if (0 <= dotPos) {
                    return filename.substring(dotPos + 1);
                }
            }
        }

        return "";
    }

    /**
     * Return the MIME type for the given extension.
     *
     * @param extension A file extension without the leading '.'
     * @return The MIME type for the given extension or null if there is none.
     */
    public static String getMimeTypeFromExtension(String extension) {
        if (extension != null && extension.length() > 0) {
            return sMimeTypeMap.get(extension);
        }

        return null;
    }

    public static String getFileExtension(String fileName) {
        Preconditions.checkArgument(!ValueUtil.isEmpty(fileName));
        int dotPos = fileName.lastIndexOf('.');
        if (0 <= dotPos) {
            return fileName.substring(dotPos + 1);
        }
        return fileName;
    }

    private final String mimeType;
    private final String charset;
//    /**
//     * File fileName or extension. Such as "aa.txt" or just "txt".
//     * @param fileName
//     */
//    public MimeType(String fileName) {
//        Preconditions.checkArgument(!ValueUtil.isEmpty(fileName));
//        charset = DEFAULT_CHARSET;
//        String extension = getFileExtension(fileName);
//        if(ValueUtil.isEmpty(extension)) {
//            mimeType = DEFAULT_MIME_TYPE;
//            return;
//        }
//        String mimeType = getMimeTypeFromExtension(extension);
//        if(ValueUtil.isEmpty(mimeType)) {
//            this.mimeType = DEFAULT_MIME_TYPE;
//            return;
//        }
//        this.mimeType = mimeType;
//    }

    public MimeType(String type, String charset) {
        if(ValueUtil.isEmpty(type)) type = DEFAULT_MIME_TYPE;
        if(ValueUtil.isEmpty(charset)) charset = DEFAULT_CHARSET;
        this.mimeType = type;
        this.charset = charset;
    }

    //Content-Type: application/x-www-form-urlencoded//Content-Type: text/html; charset=utf-8
//    public MimeType(String ) {
//
//    }

    public final String getMimeType(){
        return mimeType;
    }

    public final String getCharset(){
        return charset;
    }

    public final String getContentType(){
        return mimeType + "; " + charset;
    }

    public static MimeType createFromFileName(String fileName){
        Preconditions.checkArgument(!ValueUtil.isEmpty(fileName));
        String mimeType = DEFAULT_MIME_TYPE;
        String extension = getFileExtension(fileName);
        if(ValueUtil.isEmpty(extension)) {
            return new MimeType(DEFAULT_MIME_TYPE, null);
        }
        mimeType = getMimeTypeFromExtension(extension);
        if(ValueUtil.isEmpty(mimeType)) {
            return new MimeType(DEFAULT_MIME_TYPE, null);
        }
        return new MimeType(mimeType, DEFAULT_CHARSET);
    }

    /**
     * Create {@link MimeType} by file extension, such as zip, json,txt.
     * If user kown the specify file will be upload or
     * @param extension
     * @param charset
     * @return
     */
    public static MimeType createFromExtension(String extension, String charset){
        Preconditions.checkArgument(!ValueUtil.isEmpty(extension));
        String mimeType = getMimeTypeFromExtension(extension);
        if(ValueUtil.isEmpty(mimeType)) {
            return new MimeType(DEFAULT_MIME_TYPE, null);
        }
        return new MimeType(mimeType, charset);
    }

    public static MimeType createUrlEncode(){
        return new MimeType(DEFAULT_URL_MIME_TYPE, DEFAULT_CHARSET);
    }

    public static MimeType createUrlEncode(String charset) {
        if(ValueUtil.isEmpty(charset)) charset = DEFAULT_CHARSET;
        return new MimeType(DEFAULT_URL_MIME_TYPE, charset);
    }

    public static MimeType defaultMimeType(){
        return new MimeType(DEFAULT_MIME_TYPE, null);
    }

    /**
     * Create the mimeType when get the response "Content-Type: text/html; charset=utf-8"
     * @param contentType
     * @return
     */
    public static MimeType crateFromContentType(String contentType){
        if(ValueUtil.isEmpty(contentType)) {
            return new MimeType(DEFAULT_MIME_TYPE, null);
        }
        String[] strings = contentType.split(CONNECTOR);
        if(strings.length < 2) return new MimeType(contentType, null);

        return new MimeType(strings[0], strings[1]);
    }

    /**
     * create a MimeType by primary type and sub type.
     * @param primaryType the primary type such as "application"
     * @param subType  the sub type such as "json"
     * @return
     */
    public static MimeType create(String primaryType, String subType){
        Preconditions.checkArgument(isValidToken(primaryType));
        Preconditions.checkArgument(isValidToken(subType));

        primaryType = primaryType.toLowerCase(Locale.ENGLISH);
        subType = subType.toLowerCase(Locale.ENGLISH);

        return new MimeType(primaryType+"/"+subType, null);
    }

    private static boolean isTokenChar(char c) {
        return c > 32 && c < 127 && "()<>@,;:/[]?=\\\"".indexOf(c) < 0;
    }

    private static boolean isValidToken(String s) {
        int len = s.length();
        if(len > 0) {
            for(int i = 0; i < len; ++i) {
                char c = s.charAt(i);
                if(!isTokenChar(c)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
