package jp.ne.monk.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Page01
 */
@WebServlet("/Page01")
public class Page01 extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Page01() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
        log("doGet()");

        ServletContext sc = getServletContext();
        sc.setAttribute("test", "Page01");
        sc.setAttribute("request", request);
        sc.setAttribute("response", response);

        String disp = "/WEB-INF/jsp/Page01.jsp";
        RequestDispatcher dispatch = request.getRequestDispatcher(disp);
        dispatch.include(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
        log("doPost()");
		doGet(request, response);
	}

}
