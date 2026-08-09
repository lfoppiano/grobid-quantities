package org.grobid.core.utilities;

import nu.xom.Attribute;
import nu.xom.Element;
import org.grobid.core.data.Measurement;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.engines.training.QuantityTrainingFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import static org.grobid.core.document.xml.XmlBuilderUtils.teiElement;

public class TeiUtils {

    private static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";

    private static final QuantityTrainingFormatter FORMATTER = new QuantityTrainingFormatter();

    /**
     * Serialises measurements as a TEI document, with the measures annotated inline in the text.
     * The notation is the one of the annotated corpus and of the annotation guidelines, e.g.
     * <pre>&lt;measure type="value"&gt;&lt;num&gt;10&lt;/num&gt; &lt;measure type="MASS"
     * unit="kg"&gt;kg&lt;/measure&gt;&lt;/measure&gt;</pre>
     * <p>
     * The text must be the one the measurements were extracted from, offsets included: see
     * {@link org.grobid.core.engines.QuantityParser#preprocess(String)}.
     */
    public static String toTei(List<Measurement> measurements, String text) {
        Element root = getQuantitiesTEIHeader(-1);

        Element textNode = teiElement("text");
        // for the moment we support english only
        textNode.addAttribute(new Attribute("xml:lang", XML_NAMESPACE, "en"));
        textNode.appendChild(FORMATTER.trainingExtraction(measurements, text));

        root.appendChild(textNode);

        return XmlBuilderUtils.toXml(root);
    }

    public static Element getQuantitiesTEIHeader(int id) {
        Element tei = teiElement("tei");
        Element teiHeader = teiElement("teiHeader");

        if (id != -1) {
            Element fileDesc = teiElement("fileDesc");
            fileDesc.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", "_" + id));
            teiHeader.appendChild(fileDesc);
        }

        Element encodingDesc = teiElement("encodingDesc");

        Element appInfo = teiElement("appInfo");

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        df.setTimeZone(tz);
        String dateISOString = df.format(new java.util.Date());

        Element application = teiElement("application");
        application.addAttribute(new Attribute("version", GrobidProperties.getVersion()));
        application.addAttribute(new Attribute("ident", "GROBID"));
        application.addAttribute(new Attribute("when", dateISOString));

        Element ref = teiElement("ref");
        ref.addAttribute(new Attribute("target", "https://github.com/kermitt2/grobid"));
        ref.appendChild("A machine learning software for extracting information from scholarly documents");

        application.appendChild(ref);
        appInfo.appendChild(application);
        encodingDesc.appendChild(appInfo);
        teiHeader.appendChild(encodingDesc);
        tei.appendChild(teiHeader);

        return tei;
    }
}
