import React, { createContext } from "react";
import ReactDOM from "react-dom";
import { HashRouter, Route, Routes } from "react-router-dom";
import { MantineProvider, Title, Center, Loader, Alert } from "@mantine/core";
import { QueryClient, QueryClientProvider, useQuery } from "react-query";

import { ModalsProvider } from "@mantine/modals";
import SessionQuery from "./services/sessionQuery";
import PharmacyHomePage from "./pages/pharmacy/PharmacyHomePage";
import PharmacyDashboardPage from "./pages/pharmacy/PharmacyDashboardPage";
import InventoryFormPage from "./pages/pharmacy/inventories/InventoryFormPage";
import ReceptionPage from "./pages/pharmacy/receptions/ReceptionPage";
import ReceptionFormPage from "./pages/pharmacy/receptions/ReceptionFormPage";
import TransferPage from "./pages/pharmacy/transfers/TransferPage";
import TransferFormPage from "./pages/pharmacy/transfers/TransferFormPage";
import DispensationPage from "./pages/pharmacy/dispensations/DispensationPage";
import DispensationFormPage from "./pages/pharmacy/dispensations/DispensationFormPage";
import ConfigurationPage from "./pages/pharmacy/configurations/ConfigurationPage";
import InventoryPage from "./pages/pharmacy/inventories/InventoryPage";

const queryClient = new QueryClient();

export const UserContext = createContext<any>({});

const App = () => {
  const { data, isLoading } = useQuery(
    ["session"],
    async () => {
      return await SessionQuery.authenticate();
    },
    {
      refetchOnMount: false,
      refetchOnWindowFocus: false,
    }
  );

  const userInfo = data ? data : {};
  console.log(data);

  return (
    <>
      {isLoading ? (
        <Center style={{ width: "100%", height: "80vh" }}>
          <Loader size={"xl"} />
        </Center>
      ) : (
        <UserContext.Provider value={userInfo}>
          <HashRouter>
            <div>
              {data && data.sessionLocation === null && (
                <Alert color={"red"} variant={"filled"}>
                  <Center>
                    <Title order={3}>
                      Demandez à votre administrateur de bien vouloir configurer
                      votre Site par défaut !
                    </Title>
                  </Center>
                </Alert>
              )}
              <Routes>
                <Route path="/supply" element={<PharmacyHomePage />}>
                  <Route path="" element={<PharmacyDashboardPage />} />
                  <Route path="inventory" element={<InventoryPage />} />
                  <Route
                    path="inventory/:inventoryId"
                    element={<InventoryFormPage />}
                  />
                  <Route path="reception" element={<ReceptionPage />} />
                  <Route
                    path="reception/:receptionId"
                    element={<ReceptionFormPage />}
                  />
                  <Route path="transfer" element={<TransferPage />} />
                  <Route
                    path="transfer/:transferId/:type"
                    element={<TransferFormPage />}
                  />
                  <Route path="dispensation" element={<DispensationPage />} />
                  <Route
                    path="dispensation/edit/:identifier/:type/:dispensationId"
                    element={<DispensationFormPage />}
                  />
                  <Route
                    path="dispensation/view/:identifier/:type"
                    element={<DispensationFormPage />}
                  />
                  <Route path="parameter" element={<ConfigurationPage />} />
                </Route>
                <Route path="*" element={<PharmacyDashboardPage />} />
              </Routes>
            </div>
          </HashRouter>
        </UserContext.Provider>
      )}
    </>
  );
};

ReactDOM.render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <MantineProvider>
        <ModalsProvider>
          <App />
        </ModalsProvider>
      </MantineProvider>
      {/*<ReactQueryDevtools initialIsOpen={false} />*/}
    </QueryClientProvider>
  </React.StrictMode>,
  document.getElementById("root")
);
