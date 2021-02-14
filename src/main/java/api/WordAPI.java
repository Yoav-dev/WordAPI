package api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import infra.FormattedCharacter;
import infra.WordFile;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class WordAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Random RANDOM = new Random();
	
	private HashMap<Long, WordFile> wordFiles = new HashMap<Long, WordFile>();
	
	private static String generateHTML(FormattedCharacter [] content) {
		StringBuilder stringBuilder = new StringBuilder();
		boolean openItalic = false;
		boolean openBold = false;
		boolean openUnderline = false;
		
		for (FormattedCharacter formattedCharacter : content) {
			if ((!formattedCharacter.isUnderline()) && openUnderline) {
				stringBuilder.append("</u>");
				openUnderline = false;
			}
			
			if ((!formattedCharacter.isBold()) && openBold) {
				stringBuilder.append("</b>");
				openBold = false;
			}
			
			if ((!formattedCharacter.isItalic()) && openItalic) {
				stringBuilder.append("</i>");
				openItalic = false;
			}
			
			if (formattedCharacter.isItalic() && !openItalic) {
				stringBuilder.append("<i>");
				openItalic = true;
			}
			
			if (formattedCharacter.isBold() && !openBold) {
				stringBuilder.append("<b>");
				openBold = true;
			}
			
			if (formattedCharacter.isUnderline() && !openUnderline) {
				stringBuilder.append("<u>");
				openUnderline = true;
			}
			
			stringBuilder.append(formattedCharacter.getCharacter());
		}
		
		if (content.length > 0) {
			if (content[content.length - 1].isUnderline() && openUnderline) {
				stringBuilder.append("</u>");
				openUnderline = false;
			}
			
			if (content[content.length - 1].isBold() && openBold) {
				stringBuilder.append("</b>");
				openBold = false;
			}
			
			if (content[content.length - 1].isItalic() && openItalic) {
				stringBuilder.append("</i>");
				openItalic = false;
			}
		}
		
		return stringBuilder.toString();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String operation = request.getParameter("op");
		
		if (operation.equals("create")) {
			long id;
			
			do {
				id = Math.abs(RANDOM.nextLong());
			} while (wordFiles.containsKey(id));
			
			wordFiles.put(id, new WordFile());
			response.getWriter().append(String.valueOf(id));
			
			return;
		}
		
		WordFile wordFile = wordFiles.get(Long.valueOf(request.getParameter("id")));
		
		if (operation.equals("getContent")) {
			FormattedCharacter [] content = wordFile.getContent();
			JsonArray arr = new JsonArray();
			
			for (FormattedCharacter formattedCharacter : content) {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("char", formattedCharacter.getCharacter());
				jsonObject.addProperty("italic", formattedCharacter.isItalic());
				jsonObject.addProperty("bold", formattedCharacter.isBold());
				jsonObject.addProperty("underline", formattedCharacter.isUnderline());
				arr.add(jsonObject);
			}
			
			response.getWriter().append(new Gson().toJson(arr));
			
			return;
		}
		
		if (operation.equals("add")) {
			String content = request.getParameter("content");
			String positionStr = request.getParameter("position");
			
			if (positionStr == null) {
				wordFile.add(content);
			} else {
				wordFile.add(content, Integer.valueOf(positionStr));
			}
		} else if (operation.equals("italic")) {
			int start = Integer.valueOf(request.getParameter("start"));
			int end = Integer.valueOf(request.getParameter("end"));

			wordFile.italic(start, end);	
		} else if (operation.equals("bold")) {
			int start = Integer.valueOf(request.getParameter("start"));
			int end = Integer.valueOf(request.getParameter("end"));

			wordFile.bold(start, end);	
		} else if (operation.equals("underline")) {
			int start = Integer.valueOf(request.getParameter("start"));
			int end = Integer.valueOf(request.getParameter("end"));

			wordFile.underline(start, end);	
		} else if (operation.equals("remove")) {
			int start = Integer.valueOf(request.getParameter("start"));
			int end = Integer.valueOf(request.getParameter("end"));

			wordFile.remove(start, end);	
		} else if (operation.equals("undo")) {
			wordFile.undo();
		} else if (operation.equals("redo")) {
			wordFile.redo();
		}
	
		response.setContentType("text/html");
		response.getWriter().append(generateHTML(wordFile.getContent()));
	}
}