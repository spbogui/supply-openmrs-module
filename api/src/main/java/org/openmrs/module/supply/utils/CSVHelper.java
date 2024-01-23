package org.openmrs.module.supply.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.*;
import org.openmrs.module.supply.api.ProductService;
//import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSVHelper {
	
	//    public static String TYPE = "text/csv";
	public static final String CSV_APP_TYPE = "application/vnd.ms-excel";
	
	public static final String CSV_TEXT_TYPE = "text/csv";
	
	static String[] HEADERs = { "#Code", "#Designation", "#Designation de dispensation", "#Unite de conditionnement",
	        "#Nombre unite", "#Unite de dispensation", "#Prix de vente", "#Programme" };
	
	//	public static boolean hasCSVFormat(MultipartFile file) {
	//		System.out.println("----------------------------------------> " + file.getContentType());
	//		return CSV_APP_TYPE.equals(file.getContentType()) || CSV_TEXT_TYPE.equals(file.getContentType());
	//	}
	
	public static List<Product> csvProductRegimes(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim().withDelimiter(';'))) {

            List<Product> products = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            int index = -1;
            for (CSVRecord csvRecord : csvRecords) {
                Product product;
                if (csvRecord.get("#Regime").equals("*")) {
                    product = productService().getProductByCode(csvRecord.get("#Code"));
                    if (product != null) {
                        List<ProductRegime> regimens = productService().getAllProductRegimes();
//                        for (ProductRegime regimen : regimens) {
//                            if (!product.getRegimes().contains(regimen)) {
//                                product.addRegime(regimen);
//                            }
//                        }
                        products.add(product);
                    }
                } else {
                    ProductRegime regimen = productService().getProductRegimeByConceptName(csvRecord.get("#Regime"));
                    if (regimen == null) {
                        continue;
                    }
                    product = containsCode(products, csvRecord.get("#Code"));
                    if (product == null) {
                        product = productService().getProductByCode(csvRecord.get("#Code"));
                        if (product == null) {
                            break;
                        }
                    } else {
                        index = products.indexOf(product);
                    }

//                    if (!product.getRegimes().contains(regimen)) {
//                        product.addRegime(regimen);
//                    }
                    if (index == -1) {
                        products.add(product);
                    } else {
                        products.set(index, product);
                    }
                }

            }

            return products;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
    }
	
	public static List<Product> csvProducts(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim().withDelimiter(';'))) {

            List<Product> products = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            String message = "";

            int count = 0;
//            for (CSVRecord csvRecord : csvRecords) {
//                ProductProgram program = productService().getProductProgramByName(csvRecord.get("#Programme"));
//                Product product = containsCode(products, csvRecord.get("#Code"));
//                if (product != null) {
//                    products.remove(product);
//                    product.addProgram(program);
//                    products.add(product);
//                    message = "added program to existing in list : " + csvRecord.get("#Code");
//                } else {
//                    product = productService().getProductByCode(csvRecord.get("#Code"));
//                    if (product == null) {
//                        product = getNewProduct(csvRecord);
//                        product.addProgram(program);
//                        products.add(product);
//                        message = "added program to new in all : " + csvRecord.get("#Code");
//                    } else {
//                        if (!product.getPrograms().contains(program)) {
//                            product.addProgram(program);
//                            products.add(product);
//                            message = "added program to existing in db : " + csvRecord.get("#Code");
//                        }
//                    }
//                }
//                System.out.println("--------- Product [" + message + "]: " + ++count);
//            }

            return products;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
    }
	
	private static Product getNewProduct(CSVRecord csvRecord) {
		StringBuffer nameUuid = new StringBuffer("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN").replace(0, csvRecord.get("#Code")
		        .length(), csvRecord.get("#Code"));
		StringBuffer productUuid = new StringBuffer("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP").replace(0,
		    csvRecord.get("#Code").length(), csvRecord.get("#Code"));
		
		Product product = new Product();
		//		product.setCode(csvRecord.get("#Code"));
		ProductName dispensationName = new ProductName(csvRecord.get("#Designation de dispensation"));
		dispensationName.setUnit(productService().getProductUnitByName(csvRecord.get("#Unite de dispensation")));
		dispensationName.setUuid(nameUuid + "D");
		product.addDispensationName(dispensationName);
		
		ProductName packagingName = new ProductName(csvRecord.get("#Designation"));
		packagingName.setUnit(productService().getProductUnitByName(csvRecord.get("#Unite de conditionnement")));
		packagingName.setUuid(nameUuid + "P");
		product.addPackagingName(packagingName);
		
		product.setConversionUnit(Double.parseDouble(csvRecord.get("#Nombre unite")));
		product.setUuid(productUuid.toString());
		return product;
	}
	
	private static Product containsCode(List<Product> products, String code) {
		//		for (Product product : products) {
		//			if (product.getCode().equals(code)) {
		//				return product;
		//			}
		//		}
		return null;
	}
	
	private static ProductService productService() {
		return Context.getService(ProductService.class);
	}
	
}
