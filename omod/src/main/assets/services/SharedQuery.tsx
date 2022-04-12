import { Encounter, Settings } from "../models/shared";
import apiClient from "../utils/http-common";

const findPatient = async (identifier: string, params: string = "v=full") => {
  const response = await apiClient.get<any>(
    `/patient?identification=${identifier}${params}`
  );
  return response.data.results;
};

const createProvider = async (provider: any) => {
  const response = await apiClient.post<any>(`/provider`);
  return response.data.results;
};

const createPerson = async (person: any) => {
  const response = await apiClient.post<any>(`/person`);
  return response.data.results;
};

const findProviders = async (identifier: string, params: string = "v=full") => {
  const response = await apiClient.get<any>(`/provider?${params}`);
  return response.data.results;
};

const findEncounter = async (
  uuid: string,
  params: string
): Promise<Encounter> => {
  const response = await apiClient.get<Encounter>(
    `/encounter/${uuid}?${params}`
  );
  return response.data;
};

const findSupplySettings = async (): Promise<Settings[]> => {
  const response = await apiClient.get<any>(
    "/systemsetting?v=custom:(property,value)&q=supplyInfo"
  );
  return response.data.results;
};

const SharedQuery = {
  findPatient,
  findEncounter,
  findProviders,
  createProvider,
  createPerson,
  findSupplySettings,
};

export default SharedQuery;
