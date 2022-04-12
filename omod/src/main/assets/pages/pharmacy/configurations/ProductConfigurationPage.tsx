import { faList } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  Alert,
  Badge,
  Button,
  Card,
  Center,
  Divider,
  Grid,
  Group,
  Input,
  Loader,
  Progress,
  Select,
  SelectItem,
  Text,
  Title,
  useMantineTheme,
} from "@mantine/core";
import { useInputState } from "@mantine/hooks";
import { useEffect, useMemo, useState } from "react";
import readXlsxFile from "read-excel-file";
import { PRODUCT_COLUMNS } from "../../../components/tables/columns/product";
import CustomTable from "../../../components/tables/CustomTable";
import { useUserContext } from "../../../hooks/context";
import {
  useFindAllProducts,
  useFindProduct,
  useGetRegimes,
  useProductMutation,
} from "../../../hooks/product";
import { ProductRegime, ProductSave } from "../../../models/Product";

const ProductConfigurationPage = () => {
  const [count, setCount] = useState<number>(0);
  const [countUpdate, setCountUpdate] = useState<number>(0);
  const [countAdd, setCountAdd] = useState<number>(0);
  const [products, setProducts] = useState<ProductSave[]>([]);
  const [updatedProducts, setUpdateProducts] = useState<ProductSave[]>([]);
  const [total, setTotal] = useState<number>(1);

  const [loading, setLoading] = useState<boolean>(false);
  const [selectedProduct, setSelectProduct] = useInputState<string>("");

  const theme = useMantineTheme();
  const { userLocation } = useUserContext();

  const { productRegimes } = useGetRegimes();

  const { product, getProduct } = useFindProduct(selectedProduct);

  //   const regimes: { uuid: string; name: string } = productRegime
  //     ? productRegime.map((r: ProductRegime) => {
  //         return { uuid: r.uuid, name: r.concept.name as string };
  //       })
  //     : [];

  //   console.log(productRegime);

  const {
    products: productsFound,
    getProducts,
    isLoading,
  } = useFindAllProducts();
  const existingCodes: string[] = productsFound
    ? productsFound.map((p) => p.code)
    : [];

  const productSelectList: SelectItem[] = productsFound
    ? productsFound.map((p) => {
        return { value: p.uuid, label: p.code + " - " + p.dispensationName };
      })
    : [];

  const { createProduct } = useProductMutation();

  const onChange = (e: any) => {
    if (products.length === 0) {
      readXlsxFile(e.target.files[0]).then((rows) => {
        const productsToSave: ProductSave[] = [];
        // ['#Code', '#Designation', '#Designation de dispensation', '#Unite de conditionnement', '#Nombre unite', '#Unite de dispensation', '#Prix de vente', '#Programme']
        rows.forEach((row) => {
          if (row[0] !== "#Code") {
            const idx = productsToSave.findIndex(
              (p) => p.code === row[0].toString()
            );
            const program =
              row[7].toString().replaceAll("_", "").replaceAll(" ", "") +
              "PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP".slice(
                row[7].toString().replaceAll("_", "").replaceAll(" ", "").length
              );

            const regimes: string[] = [];

            const regimesData = row[8] ? row[8].toString() : null;
            if (regimesData) {
              if (regimesData === "*") {
                regimes.push(...productRegimes.map((r) => r.uuid));
              } else {
                regimes.push(
                  ...productRegimes.reduce(
                    (acc: string[], r: ProductRegime) => {
                      if (
                        regimesData
                          .split(",")
                          .includes(r.concept.display.replaceAll(" ", "/"))
                      ) {
                        acc.push(r.uuid);
                      }
                      return acc;
                    },
                    []
                  )
                );
              }
            }

            if (idx === -1) {
              const product: ProductSave = {
                code: row[0].toString(),
                conversionUnit: parseFloat(row[4].toString()),
                names: [
                  {
                    productNameType: "PACKAGING",
                    unit:
                      row[3].toString().replaceAll("/", "") +
                      "UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU".slice(
                        row[3].toString().replaceAll("/", "").length
                      ),
                    name: row[1].toString(),
                    uuid:
                      row[0].toString() +
                      "PACKKAGINGNNNNNNNNNNNNNNNNNNNNNNNNNNNN".slice(
                        row[0].toString().length
                      ),
                  },
                  {
                    productNameType: "DISPENSATION",
                    unit:
                      row[5].toString().replaceAll("/", "") +
                      "UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU".slice(
                        row[5].toString().replaceAll("/", "").length
                      ),
                    name: row[2].toString(),
                    uuid:
                      row[0].toString() +
                      "DISPENSATIONNNNNNNNNNNNNNNNNNNNNNNNNNN".slice(
                        row[0].toString().length
                      ),
                  },
                ],
                regimes,
                prices: [
                  {
                    program,
                    salePrice: parseFloat(row[6].toString()),
                    active: true,
                    location: userLocation.uuid,
                  },
                ],
                programs: [program],
                uuid:
                  row[0].toString() +
                  "PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP".slice(
                    row[0].toString().length
                  ),
              };

              //   if (row[1].toString() !== row[2].toString()) {
              //     product.names.push();
              //   }

              productsToSave.push(product);
            } else {
              const product = productsToSave.find(
                (p) => p.code === row[0].toString()
              );
              if (product) {
                product.programs.push(program);
                // product.prices.push({
                //   program,
                //   salePrice: parseFloat(row[6].toString()),
                //   active: true,
                //   location: userLocation.uuid,
                // });
                productsToSave.splice(idx, 1);
                productsToSave.push(product);
              }
            }
          }
        });
        // console.log(productsToSave);
        setTotal(productsToSave.length);
        setProducts(productsToSave);
      });
    }
  };

  const columns = useMemo(() => PRODUCT_COLUMNS, []);

  useEffect(() => {
    setCount(Math.floor(((countAdd + countUpdate) / total) * 100));
    if (selectedProduct !== "") {
      getProduct();
    }
  }, [countAdd, countUpdate, total, selectedProduct]);

  const saveProducts = () => {
    setLoading(true);
    products.forEach((product) => {
      if (!existingCodes.includes(product.code)) {
        createProduct(product, {
          onSuccess: () => {
            // setCountAdd((c) => c + 1);
            setLoading(false);
            getProducts();
          },
        });
      } /* else {
        updateProduct(
          { value: product, uuid: product.uuid },
          {
            onSuccess: () => {
              setCountUpdate((c) => c + 1);
            },
          }
        );
      } */
    });
  };

  return (
    <Card
      style={{
        border: 1,
        borderStyle: "solid",
        borderColor: theme.colors.blue[1],
      }}
      mt={"xs"}
    >
      <Card.Section>
        <Group
          m={"xs"}
          position="apart"
          style={{ marginBottom: 5, marginTop: theme.spacing.sm }}
        >
          <Group>
            <FontAwesomeIcon
              icon={faList}
              size={"1x"}
              color={theme.colors.blue[9]}
            />
            <Text color={"blue"} weight={"bold"} transform={"uppercase"}>
              Getion des produits
            </Text>
          </Group>
        </Group>
      </Card.Section>
      <Card.Section>
        <Divider my={"xs"} color={theme.colors.blue[1]} />
      </Card.Section>

      <Card
        style={{
          border: 1,
          borderStyle: "solid",
          borderColor: theme.colors.blue[1],
        }}
      >
        <Group>
          <input type="file" onChange={onChange} />
          {/* <Progress
            style={{ width: "50%", height: 30 }}
            value={count}
            label={count + "%"}
            size="xl"
          /> */}

          <Button
            loading={count !== total && count !== 0}
            onClick={saveProducts}
            color={"green"}
            disabled={products.length === 0}
          >
            Importer produits
          </Button>
          {loading && <Loader size={"xl"} />}
        </Group>
      </Card>
      <Card
        style={{
          border: 1,
          borderStyle: "solid",
          borderColor: theme.colors.blue[1],
        }}
      >
        <Grid columns={10}>
          <Grid.Col span={2}>
            <Card
              style={{
                border: 1,
                borderStyle: "solid",
                borderColor: theme.colors.blue[1],
              }}
            >
              <Group>
                <FontAwesomeIcon
                  icon={faList}
                  size={"1x"}
                  color={theme.colors.blue[9]}
                />
                <Text color={"blue"} weight={"bold"} transform={"uppercase"}>
                  Liste des programmes
                </Text>
              </Group>

              <Card.Section>
                <Divider color={"blue"} size={"sm"} my={"sm"} />
              </Card.Section>
            </Card>
          </Grid.Col>
          <Grid.Col span={8}>
            <Card
              style={{
                border: 1,
                borderStyle: "solid",
                borderColor: theme.colors.blue[1],
              }}
            >
              <Group position="apart">
                <Group>
                  <FontAwesomeIcon
                    icon={faList}
                    size={"1x"}
                    color={theme.colors.blue[9]}
                  />
                  <Text color={"blue"} weight={"bold"} transform={"uppercase"}>
                    Produits
                  </Text>
                </Group>
                <Group>
                  <Text size="sm" color={"gray"} weight="bold">
                    Total
                  </Text>
                  <Badge size="lg" color={"green"}>
                    {productsFound ? productsFound.length : "0"}
                  </Badge>
                </Group>
              </Group>

              <Card.Section>
                <Divider color={"blue"} size={"sm"} my={"sm"} />
              </Card.Section>
              <Card
                style={{
                  border: 1,
                  borderStyle: "solid",
                  borderColor: theme.colors.blue[1],
                }}
              >
                <Group>
                  {isLoading ? (
                    <Center style={{ height: "5vh" }}>
                      <Loader />
                    </Center>
                  ) : (
                    <>
                      {productsFound.length > 0 ? (
                        <>
                          <Text>Sélectionner un produit dans la liste : </Text>
                          <Select
                            style={{ width: "60%" }}
                            data={productSelectList}
                            searchable
                            clearable
                            value={selectedProduct}
                            onChange={setSelectProduct}
                          />

                          {product && (
                            <Card
                              style={{
                                border: 1,
                                borderStyle: "solid",
                                borderColor: theme.colors.green[1],
                              }}
                            >
                              <Group>
                                <Text size="lg">CODE :</Text>
                                <Text
                                  size="xl"
                                  color={theme.colors.green[9]}
                                  weight={"bold"}
                                >
                                  {product.code}
                                </Text>
                              </Group>
                              <Group>
                                <Text size="lg">Désignation :</Text>
                                <Text
                                  size="xl"
                                  color={theme.colors.green[9]}
                                  weight={"bold"}
                                >
                                  {product.packagingName}
                                </Text>
                              </Group>
                              <Group>
                                <Text size="lg">
                                  Désignation de dispensation :
                                </Text>
                                <Text
                                  size="xl"
                                  color={theme.colors.green[9]}
                                  weight={"bold"}
                                >
                                  {product.dispensationName}
                                </Text>
                              </Group>
                              <Group>
                                <Text size="lg">Unité de conversion :</Text>
                                <Text
                                  size="xl"
                                  color={theme.colors.green[9]}
                                  weight={"bold"}
                                >
                                  ({product.conversionUnit})
                                </Text>
                              </Group>
                              <Group>
                                <Text size="lg">Programmes associés :</Text>
                                <Text
                                  size="xl"
                                  color={theme.colors.green[9]}
                                  weight={"bold"}
                                >
                                  {product.programs?.map((p) => (
                                    <Badge size="xl" key={p.uuid}>
                                      {p.name}
                                    </Badge>
                                  ))}
                                </Text>
                              </Group>
                              <Group>
                                <Text size="lg">Régimes associés :</Text>
                                <Text
                                  size="xl"
                                  color={theme.colors.green[9]}
                                  weight={"bold"}
                                >
                                  {product.regimes?.map((p) => (
                                    <Badge size="xl" key={p.uuid}>
                                      {p.concept.display}
                                    </Badge>
                                  ))}
                                </Text>
                              </Group>
                            </Card>
                          )}
                        </>
                      ) : (
                        <Alert color={"red"} mt={"xs"}>
                          <Center>
                            Vous n'avez aucun produit veuillez procéder à
                            l'importation SVP
                          </Center>
                        </Alert>
                      )}
                    </>
                  )}
                </Group>
              </Card>
            </Card>
          </Grid.Col>
        </Grid>
      </Card>
    </Card>
  );
};

export default ProductConfigurationPage;
