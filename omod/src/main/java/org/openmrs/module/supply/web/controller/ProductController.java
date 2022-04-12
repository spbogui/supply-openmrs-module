package org.openmrs.module.supply.web.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.supply.Product;
import org.openmrs.module.supply.api.ProductService;
import org.openmrs.module.supply.utils.CSVHelper;
import org.openmrs.web.WebConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller("${rootrootArtifactid}.ProductController")
@RequestMapping("module/supply/product")
public class ProductController {
	
	@Autowired
	private ProductService productService;
	
	public final String VIEW = "module/supply/product";
	
	public final String VIEW_LIST = VIEW + "/list";
	
	public final String VIEW_DETAIL = VIEW + "/detail";
	
	public final String VIEW_UPLOAD = VIEW + "/upload";
	
	@RequestMapping(value = VIEW_LIST + ".form", method = RequestMethod.GET)
	public String onGetList(ModelMap modelMap) {
		if (Context.isAuthenticated()) {
			modelMap.addAttribute("products", productService.getAllProducts(false));
			return VIEW_LIST;
		}
		return null;
	}
	
	@RequestMapping(value = VIEW_DETAIL + ".form", method = RequestMethod.GET)
	public String onGetDetail(ModelMap modelMap, @RequestParam(value = "id") Integer id) {
		if (Context.isAuthenticated()) {
			if (id != 0) {
				modelMap.addAttribute("product", productService.getProduct(id));
				return VIEW_DETAIL;
			}
		}
		return null;
	}
	
	/**
	 * @param request
	 * @param file
	 * @return String
	 */
	@RequestMapping(value = VIEW_UPLOAD + ".form", method = RequestMethod.GET)
	public String onUpload(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
		if (Context.isAuthenticated()) {
			HttpSession session = request.getSession();
			String message = "";
			if (CSVHelper.hasCSVFormat(file)) {
				try {
					List<Product> reports = productService.uploadProduct(file);
					message = "Produits importés avec succès";
					session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, message + ". [" + reports.size() + "]");
				}
				catch (Exception e) {
					message = "Could not upload the file : " + file.getOriginalFilename() + " : " + e.getMessage();
					System.out.println("---------------------" + e.getMessage());
					session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, message);
				}
			} else {
				session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "S'il vous plait importez un fichier CSV !");
			}
		}
		return VIEW_LIST;
	}
}
