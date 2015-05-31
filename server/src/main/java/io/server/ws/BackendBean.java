package io.server.ws;

import io.server.ws.model.App;
import io.server.ws.model.AppContainer;
import io.server.ws.model.ModelGenerator;
import io.server.ws.model.ReturnCode;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The one and only backend bean.
 * 
 * @author s7n
 */
@ApplicationScoped
@LocalBean
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
	 */
	public ReturnCode update(final Long id, final String name,
			final String description, final Double price) {
		log("update/create application", id, name, description, price);
		// update
		for (final Iterator<App> ia = this.appContainer.getApps().iterator(); ia
				.hasNext();) {
			App app = ia.next();
			if (app.getId() == id) {
				app.setActivated(true);
				app.setDescription(description);
				app.setName(name);
				app.setPrice(price);
				return ReturnCode.SUCCESS;
			}
		}

		// create new entry
		try {
			final Image image = ImageIO.read(new File(ModelGenerator
					.getRessouceImage()));
			final String appUrl = ModelGenerator.generateAppUrl();
			final String checksum = UUID.randomUUID().toString();
			this.appContainer.getApps().add(
					new App(new Random().nextLong(), name, description, price,
							true, new Date(), appUrl, checksum, image));
			return ReturnCode.SUCCESS;
		} catch (final IOException e) {
			log("can not load base image", e.getMessage());
			return ReturnCode.INTERNAL_ERROR;
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
	public ReturnCode updateImage(final Long id, final byte[] image) {
		log("update application", id);
		for (final Iterator<App> ia = this.appContainer.getApps().iterator(); ia
				.hasNext();) {
			App app = ia.next();
			if (app.getId() == id) {
				app.setImage(new ImageIcon(image).getImage());
				app.setAppUrl("/apps/" + UUID.randomUUID() + ".app");
				app.setChecksum(UUID.randomUUID().toString());
				return ReturnCode.SUCCESS;
			}
		}
		return ReturnCode.OBJECT_NOT_FOUND;
	}

	/**
	 * Delete application by id.
	 * 
	 * @param id
	 * @return
	 */
	public ReturnCode delete(final Long id) {
		log("delete application", id);
		App app = this.appContainer.getApps().stream()
				.filter(e -> e.getId().equals(id)).findFirst().orElse(null);
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
