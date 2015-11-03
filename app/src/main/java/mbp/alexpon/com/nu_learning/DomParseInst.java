package mbp.alexpon.com.nu_learning;

/**
 * Created by apple on 15/6/5.
 */
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DomParseInst {

    public static List<task> ReadbookXML(InputStream inStream) throws Exception{

        List<task> books=new ArrayList<>();

        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();

        DocumentBuilder builder=factory.newDocumentBuilder();

        Document document=builder.parse(inStream);

        Element root=document.getDocumentElement();

        NodeList nodes=root.getElementsByTagName("instruction");


        for(int i=0;i<nodes.getLength();i++){

            Element bookElement=(Element)nodes.item(i);

            task book=new task();

            book.setContent(bookElement.getAttribute("content"));

            book.setTitle(bookElement.getAttribute("title"));


            books.add(book);

        }

        return books;

    }

}