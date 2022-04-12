export interface Location {
  name: string;
  description: string;
  parentLocation?: Location;
  tags: any;
  address1: string;
  address2: string;
  address3: string;
  address4: string;
  address5: string;
  address6: string;
  address7: string;
  address8: string;
  postalCode: string;
  uuid: string;
  childrenLocations: Location[];
  attributes?: any;
}

export interface LocationAttribute {
  value: any;
  attributeType: any;
  uuid: string;
  voided: boolean;
}

export interface Obs {
  obsDatetime: Date;
  concept: any;
  person: any;
  value: any;
  uuid: string;
}

export interface Encounter {
  encounterDatetime: Date;
  obs: Obs[];
  patient: any;
  location: any;
}

export interface Settings {
  property: string;
  value: any;
}
