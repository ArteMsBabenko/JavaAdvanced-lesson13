package ui;

import java.io.IOException;
import java.time.LocalDate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import dao.DAOException;
import domain.Magazine;
import domain.Subscribe;
import domain.User;
import service.MagazineService;
import service.SubscribeService;
import service.UserService;
import serviceImpl.MagazineServiceImpl;
import serviceImpl.SubscribeServiceImpl;
import serviceImpl.UserServiceImpl;

@WebServlet("/subscribe")
public class SubscribeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private UserService userService = UserServiceImpl.getUserService();
	private MagazineService magazineService = MagazineServiceImpl.getMagazineService();
	private SubscribeService subscribeService = SubscribeServiceImpl.getSubscribeService();

	private Logger log = Logger.getLogger(SubscribeServlet.class);

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.trace("Getting fields values...");
		HttpSession session = request.getSession();
		Integer userID = (Integer) session.getAttribute("userID");
		User user = null;
		try {
			log.trace("Getting user by id from database...");
			user = userService.readByID(userID);
		} catch (DAOException e) {
			log.error("Getting user by id failed!", e);
		}

		String magazineID = request.getParameter("magazineID");
		Magazine magazine = null;
		try {
			log.trace("Getting magazine by id from database...");
			magazine = magazineService.readByID(Integer.parseInt(magazineID));
		} catch (NumberFormatException | DAOException e) {
			log.error("Getting magazine by id failed!", e);
		}

		String subscribePeriod = request.getParameter("subscribePeriod");

		if (user == null || magazine == null) {
			log.warn("There is no user with id=" + userID + " or magazine with id=" + magazineID + " in database!");
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write("?? ???????? ???????????? ???????????????????????? ?? id=" + userID + " ?????? ???????????? ?? id=" + magazineID + " ??????????????????????!");
		} else {
			Subscribe subscribe = new Subscribe(user, magazine, false, LocalDate.now(), Integer.parseInt(subscribePeriod));

			try {
				log.trace("Saving subscribe in database...");
				subscribeService.insert(subscribe);
			} catch (DAOException e) {
				log.error("Creating subscribe failed!", e);
			}

			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write("Success");
		}
	}
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String id = request.getParameter("id");

		Subscribe subscribe = null;
		boolean subscribeUpdate = false;
		boolean subscribeStatus = false;

		try {
			log.trace("Getting subscribe by id from database...");
			subscribe = subscribeService.readByID(Integer.parseInt(id));
		} catch (NumberFormatException | DAOException e) {
			log.error("Getting subscribe by id failed!", e);
		}

		if (subscribe == null) {
			log.warn("There is no subscribe with id=" + id + " in database!");
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write("?? ???????? ???????????? ???????????? ?? id=" + id + " ??????????????????????!");
		} else {
			try {
				log.info("Updating subscribe by id in database...");
				if (subscribe.getSubscribeStatus()) {
					subscribeStatus = false;
				} else {
					subscribeStatus = true;
				}				
				
				subscribeUpdate = subscribeService.updateByID(new Subscribe(subscribe.getId(), subscribe.getUser(),
					subscribe.getMagazine(), subscribeStatus, subscribe.getSubscribeDate(), subscribe.getSubscribePeriod()));				
			} catch (DAOException e) {
				log.error("Updating subscribe by id failed!", e);
			}

			if (subscribeUpdate) {
				String json = new Gson().toJson(subscribeStatus);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(json);
			}
		}
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String id = request.getParameter("id");
		
		try {
			log.trace("Deleting subscribe item from database...");
			subscribeService.delete(Integer.parseInt(id));
		} catch (NumberFormatException | DAOException e) {
			log.error("Deleting subscribe failed!", e);
		}
		
		response.setContentType("text");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("Success");
	}

}