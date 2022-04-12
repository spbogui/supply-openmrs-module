import apiClient from "../utils/http-common";
import {
  Product,
  ProductAttribute,
  ProductAttributeSave,
  ProductProgram,
  ProductRegime,
  ProductSave,
} from "../models/Product";

const findProgram = async (
  uuid: string,
  params: string
): Promise<ProductProgram> => {
  const response = await apiClient.get<any>(
    `/productProgram/${uuid}?${params}`
  );
  return response.data;
};

const findAllPrograms = async (params: string): Promise<ProductProgram[]> => {
  const response = await apiClient.get<any>(`/productProgram?${params}`);
  return response.data.results;
};

const findAll = async (params: string): Promise<Product[]> => {
  const response = await apiClient.get<any>(`/product?${params}`);
  return response.data.results;
};

const findById = async (
  id: string | undefined,
  params: string
): Promise<Product | undefined> => {
  if (id) {
    const response = await apiClient.get<Product>(`/product/${id}?${params}`);
    return response.data;
  } else {
    return undefined;
  }
};

const createProductAttribute = async (
  product: ProductAttributeSave
): Promise<ProductAttribute> => {
  const response = await apiClient.post<ProductAttribute>(
    "/productAttribute",
    product
  );
  return response.data;
};

const createProduct = async (product: ProductSave): Promise<Product> => {
  const response = await apiClient.post<Product>("/product", product);
  return response.data;
};

const updateProduct = async (data: any, uuid: string): Promise<Product> => {
  const response = await apiClient.post<Product>("/product/" + uuid, data);
  return response.data;
};

const updateProductName = async (data: any): Promise<Product> => {
  const response = await apiClient.post<Product>(
    "/product/" + data.productUuid + data.uuid,
    data.name
  );
  return response.data;
};

const updateProductAttribute = async (
  data: any,
  uuid: string
): Promise<ProductAttribute> => {
  const response = await apiClient.post<ProductAttribute>(
    `/productAttribute/${uuid}`,
    data
  );
  return response.data;
};

const findAttribute = async (
  product: string,
  batchNumber: string
): Promise<ProductAttribute> => {
  const response = await apiClient.get<any>(
    `/productAttribute?product=${product}&batchNumber=${batchNumber}`
  );
  return response.data.results.length > 0 ? response.data.results[0] : null;
};

// const update = async (data: any, uuid: string | undefined): Promise<Product | undefined> => {
//     if (uuid) {
//         const response = await apiClient.post(`/product/${uuid}`, data);
//         return response.data;
//     } else {
//         return undefined;
//     }
// }

const findAllByProgram = async (
  program: string,
  filter: string = ""
): Promise<Product[]> => {
  const response = await apiClient.get<any>(
    `/product?program=${program}&${filter}`
  );
  return response.data.results;
};

const findAllByRegime = async (regime: string) => {
  const response = await apiClient.get<any>(`/product?regime=${regime}`);
  return response.data.results;
};

const findAllRegimes = async (
  params: string = ""
): Promise<ProductRegime[]> => {
  const response = await apiClient.get<any>(`/productRegime?${params}`);
  return response.data.results;
};

const ProductService = {
  findAll,
  findById,
  findAllByProgram,
  findAllByRegime,
  findAllRegimes,
  findAttribute,
  createProduct,
  updateProduct,
  updateProductName,
  findProgram,
  findAllPrograms,
  createProductAttribute,
  updateProductAttribute,
};

export default ProductService;
