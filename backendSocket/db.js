const mysql = require('mysql2/promise');
require('dotenv').config();

// Najprv vytvor pool
const pool = mysql.createPool({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  port: process.env.DB_PORT, // nezabudni pridať DB_PORT do .env, ak ho používaš
});

// Potom ho testuj
pool.getConnection()
  .then(() => console.log('✅ Pripojenie k databáze úspešné!'))
  .catch((err) => console.error('❌ Pripojenie zlyhalo:', err));

module.exports = pool;
