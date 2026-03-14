// Script de inicialização do MongoDB
// Executado automaticamente na primeira inicialização do container

// Conexão ao banco de dados principal
db = db.getSiblingDB('pharmacare');

// Criar usuário da aplicação
db.createUser({
  user: 'pharmacare_app',
  pwd: 'app123456',
  roles: [
    { role: 'readWrite', db: 'pharmacare' },
    { role: 'dbAdmin', db: 'pharmacare' },
    { role: 'clusterMonitor', db: 'admin' }
  ]
});

// Criar coleções principais do domínio
const collections = [
  'products',
  'inventory',
  'prescriptions',
  'customers',
  'suppliers',
  'branches',
  'domain_events',
  'snapshots'
];

collections.forEach(collectionName => {
  if (!db.getCollectionNames().includes(collectionName)) {
    db.createCollection(collectionName);
    print(`Coleção ${collectionName} criada com sucesso.`);
  }
});

// Criar índices para otimização
db.products.createIndex({ barcode: 1 }, { 
  unique: true, 
  name: 'idx_products_barcode_unique',
  background: true
});

db.products.createIndex({ 
  name: "text",
  description: "text",
  active_principle: "text"
}, {
  name: 'idx_products_text_search',
  weights: {
    name: 10,
    description: 5,
    active_principle: 8
  },
  default_language: "portuguese"
});

db.products.createIndex({ 
  category: 1, 
  status: 1 
}, { 
  name: 'idx_products_category_status',
  background: true 
});

db.inventory.createIndex({ 
  productId: 1, 
  branchId: 1 
}, { 
  unique: true, 
  name: 'idx_inventory_product_branch_unique',
  background: true 
});

db.inventory.createIndex({ 
  branchId: 1, 
  quantity: 1 
}, { 
  name: 'idx_inventory_branch_quantity',
  background: true 
});

db.prescriptions.createIndex({ 
  code: 1 
}, { 
  unique: true, 
  name: 'idx_prescriptions_code_unique',
  background: true 
});

db.prescriptions.createIndex({ 
  patientCpf: 1, 
  status: 1 
}, { 
  name: 'idx_prescriptions_patient_status',
  background: true 
});

db.customers.createIndex({ 
  cpf: 1 
}, { 
  unique: true, 
  name: 'idx_customers_cpf_unique',
  background: true 
});

db.customers.createIndex({ 
  email: 1 
}, { 
  unique: true, 
  name: 'idx_customers_email_unique',
  background: true,
  partialFilterExpression: { email: { $exists: true } }
});

db.domain_events.createIndex({ 
  aggregateId: 1, 
  version: 1 
}, { 
  unique: true, 
  name: 'idx_domain_events_aggregate_version',
  background: true 
});

db.domain_events.createIndex({ 
  occurredOn: -1 
}, { 
  name: 'idx_domain_events_occurredOn_desc',
  background: true 
});

db.snapshots.createIndex({ 
  aggregateId: 1 
}, { 
  unique: true, 
  name: 'idx_snapshots_aggregate_unique',
  background: true 
});

print('✅ Inicialização do MongoDB concluída com sucesso!');