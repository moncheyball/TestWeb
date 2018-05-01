package jp.ne.monk.listener;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestListener implements ServletContextAttributeListener {

	@Override
	public void attributeAdded(ServletContextAttributeEvent event) {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("attributeAdded:" + event.getName());

		if(event.getName().equals("IF")){
			show(event);
		}
	}

	@Override
	public void attributeRemoved(ServletContextAttributeEvent event) {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("attributeRemoved");

	}

	@Override
	public void attributeReplaced(ServletContextAttributeEvent event) {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("attributeReplaced:" + event.getName());

		if(event.getName().equals("IF")){
			show(event);
		}
	}

	public void show(ServletContextAttributeEvent event) {
		System.out.println("show()");

		ServletContext context = event.getServletContext();
		HttpServletRequest request = (HttpServletRequest) context.getAttribute("request");
		HttpServletResponse response = (HttpServletResponse) context.getAttribute("response");

		RequestDispatcher dispatch = context.getRequestDispatcher("/Page02");

        try {
			dispatch.include(request, response);
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
	}

}
