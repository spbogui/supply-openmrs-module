import {
  Card,
  CardSection,
  Grid,
  Table,
  Text,
  useMantineTheme,
} from "@mantine/core";
import dayjs from "dayjs";
import { ProductOperation } from "../../models/ProductOperation";
import { Fn } from "../../utils/Fn";

type RegimeTableProps = {
  dispensations: ProductOperation[];
};

type RegimeTableData = {
  regimeDate: Date;
  label: string;
};
const RegimeTable = (props: RegimeTableProps) => {
  const { dispensations } = props;
  const theme = useMantineTheme();

  const regimeTableData: RegimeTableData[] = dispensations.reduce(
    (acc: RegimeTableData[], d) => {
      const regime = Fn.extractOperationAttribute(
        d,
        "REGIMENAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
      )?.value;
      if (acc.length === 0 || acc.findIndex((a) => a.label === regime)) {
        acc.push({
          regimeDate: d.operationDate,
          label: regime,
        });
      }
      return acc;
    },
    []
  );
  return (
    <Card>
      <CardSection>
        <Text color={"blue"} size={"xs"} transform={"uppercase"}>
          Historique des changements de Régime
        </Text>
      </CardSection>
      <Grid columns={4}>
        <Grid.Col span={1}>
          {regimeTableData.map((d) => (
            <Card
              style={{
                border: 1,
                borderStyle: "solid",
                borderColor: theme.colors.gray[4],
                backgroundColor: theme.colors.gray[1],
              }}
              shadow={"xs"}
            >
              <Text color={"gray"}>
                {dayjs(d.regimeDate).format("DD/MM/YYYY")}
              </Text>
              <Text color={"blue"} size={"xl"}>
                {d.label}
              </Text>
            </Card>
          ))}
        </Grid.Col>
      </Grid>
      <Table>
        <tr>
          <th>Date</th>
          {regimeTableData.map((d) => (
            <td></td>
          ))}
        </tr>
        <tr>
          <th>Regime</th>
          {regimeTableData.map((d) => (
            <td></td>
          ))}
        </tr>
      </Table>
    </Card>
  );
};

export default RegimeTable;
