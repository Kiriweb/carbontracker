// Vehicle categories the calculator expects (from passenger_and_motorbike…json keys)
export const VEHICLE_TYPES = [
  "mini",
  "supermini",
  "lower_medium",
  "upper_medium",
  "executive",
  "luxury",
  "sports",
  "dual_purpose_4x4",
  "mpv",
  "small_car",
  "medium_car",
  "large_car",
  "average_car",
  "motorbike_small",
  "motorbike_medium",
  "motorbike_large",
  "motorbike_average",
] as const;

export const VEHICLE_FUELS = ["petrol", "diesel", "phev"] as const;

// Electricity countries from eu27_electricity_emission_factors.json
export const ELECTRICITY_COUNTRIES = [
  "Austria","Belgium","Bulgaria","Croatia","Cyprus","Czechia","Denmark","Estonia",
  "Finland","France","Germany","Greece","Hungary","Ireland","Italy","Latvia",
  "Lithuania","Luxembourg","Malta","Netherlands","Poland","Portugal","Romania",
  "Slovakia","Slovenia","Spain","Sweden",
];

// Waste types & methods derived from waste_disposal_emission_factors_extended.json keys
export const WASTE_TYPES = [
  "aggregates","average_construction","asbestos","asphalt","bricks","concrete",
  "insulation","metals","soils","mineral_oil","plasterboard","tyres","wood",
  "books","glass","clothing","household_residual_waste","organic_food_drink",
  "organic_garden","organic_mixed_food_garden","commercial_industrial_waste",
  "weee_fridges","weee_large","weee_mixed","weee_small","batteries",
  "metal_aluminium_cans","metal_mixed_cans","metal_scrap","metal_steel_cans",
  "plastics_average","plastics_film","plastics_rigid","plastics_hdpe",
  "plastics_ldpe","plastics_pet","plastics_pp","plastics_ps","plastics_pvc",
  "paper_board","paper_mixed","paper_paper",
];

export const WASTE_METHODS = [
  "open_loop","closed_loop","incineration","composting","landfill","anaerobic_digestion",
];

// Fuel types/units from fuel_combustion_emission_factors.json (split name + unit)
export const FUEL_TYPES = [
  "butane","cng","lng","lpg","natural_gas","natural_gas_100mineral","propane",
  "burning_oil","diesel","fuel_oil","gas_oil","waste_oil","coal_domestic",
];

export const FUEL_UNITS = [
  "kwh","litre","cubic_meter","tonne",
];

// Nice labels
export function labelize(s: string) {
  return s
    .replaceAll("_", " ")
    .replace(/\b(\w)/g, (m) => m.toUpperCase());
}
