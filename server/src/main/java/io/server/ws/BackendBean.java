package io.server.ws;

import io.server.ws.model.App;
import io.server.ws.model.AppContainer;
import io.server.ws.model.ModelGenerator;
import io.server.ws.model.ReturnCode;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The one and only backend bean.
 * 
 * @author s7n
 */
@ApplicationScoped
public class BackendBean implements Serializable {

	private static final long serialVersionUID = -3921199152183955121L;

	private Logger log = LoggerFactory.getLogger(BackendBean.class);

	private AppContainer appContainer;

	/**
	 * Re-/load the sample/demo application container.
	 */
	@PostConstruct
	public void init() {
		log("initialize backend");
		this.appContainer = ModelGenerator.getModel();
	}

	/**
	 * Find all available applications.
	 * 
	 * @return {@link AppService} list
	 */
	public List<App> findAll() {
		log("find all applcations");
		return this.appContainer.getApps().stream()
				.filter(e -> e.isActivated())
				.collect(Collectors.<App> toList());
	}

	/**
	 * Find all inactivated applications.
	 * 
	 * @return {@link AppService} list
	 */
	public List<App> findAllInactivated() {
		log("find all inactivated applcations");
		return this.appContainer.getApps().stream()
				.filter(e -> !e.isActivated())
				.collect(Collectors.<App> toList());
	}

	/**
	 * Find a application by name.
	 * 
	 * @param name
	 *            string
	 * @return {@link AppService} list
	 */
	public List<App> find(final String name) {
		log("find applications by name", name);
		return this.appContainer
				.getApps()
				.stream()
				.filter(e -> e.getName().toLowerCase()
						.contains(name.toLowerCase()))
				.filter(e -> e.isActivated())
				.collect(Collectors.<App> toList());
	}

	/**
	 * Get application by id.
	 * 
	 * @param id
	 *            Long
	 * @return {@link AppService} or null
	 */
	public App get(final Long id) {
		log("get application by id", id);
		return this.appContainer.getApps().stream()
				.filter(e -> e.getId().equals(id)).findFirst().orElse(null);
	}

	/**
	 * Get application image by id.
	 * 
	 * @param id
	 * @return byte[] or null;
	 */
	public Image getImage(final Long id) {
		log("get image by id", id);
		App app = get(id);
		if (app == null) {
			return null;
		}
		return app.getImage();
	}

	/**
	 * Update a existing app by id or create a new entry, without any securty
	 * permissions.
	 * 
	 * @param id
	 *            Long
	 * @param name
	 *            application name
	 * @param description
	 *            application description
	 * @param price
	 *            application price
	 * @param data
	 *            application data
	 * @return {@link ReturnCode}
	 * 
	 * @throws Exception
	 */
	public long update(final Long id, final boolean activated,
			final String name, final String description, final Double price)
			throws Exception {
		log("update/create application", id, activated, name, description,
				price);

		// update entry
		for (final Iterator<App> ia = this.appContainer.getApps().iterator(); ia
				.hasNext();) {
			App app = ia.next();
			if (app.getId() == id) {
				log("update app");
				app.setActivated(activated);
				app.setDescription(description);
				app.setName(name);
				app.setPrice(price);
				return id;
			}
		}

		// create new entry
		try {
			if (id >= 0) {
				throw new Exception(
						"id doesn't exist. create a new app with id less zero.");
			}
			log("create new app");
			final Image image = ImageIO.read(new File(ModelGenerator
					.getRessouceImage()));
			final String appUrl = ModelGenerator.generateAppUrl();
			final String checksum = UUID.randomUUID().toString();
			App newApp = new App(Long.valueOf(this.appContainer.getApps()
					.size() + 1), name, description, price, activated,
					new Date(), appUrl, checksum, image);
			List<App> newAppList = new ArrayList<>(this.appContainer.getApps());
			newAppList.add(newApp);
			this.appContainer.setApps(newAppList);
			return newApp.getId();
		} catch (final IOException e) {
			log("can not load base image", e.getMessage());
			throw new Exception("Internel Error. Could not load default image.");
		}
	}

	/**
	 * Update application image.
	 * 
	 * @param id
	 *            long
	 * @param image
	 *            byte array
	 * @return {@link ReturnCode}
	 */
	public ReturnCode updateImage(final Long id, final Image image) {
		log("update application", id);
		App app = get(id);
		if (app == null) {
			return ReturnCode.OBJECT_NOT_FOUND;
		}
		app.setImage(image);
		return ReturnCode.SUCCESS;
	}

	/**
	 * Delete application by id.
	 * 
	 * @param id
	 * @return
	 */
	public ReturnCode delete(final long id) {
		log("delete application", id);
		App app = get(id);
		if (app == null) {
			return ReturnCode.OBJECT_NOT_FOUND;
		}
		app.setActivated(false);
		return ReturnCode.SUCCESS;
	}

	private void log(final String message, final Object... parameter) {
		this.log.info(message + " : " + Arrays.toString(parameter));
	}
}
