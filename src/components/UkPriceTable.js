import React, { useEffect, useState } from 'react';
import axios from 'axios';

const UkPriceTable = () => {
  const [prices, setPrices] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axios.get('http://localhost:8080/api/data/uk_price_paid?limit=10')
      .then(response => {
        setPrices(response.data);
        setLoading(false);
      })
      .catch(error => {
        console.error('Error fetching UK Price data:', error);
        setLoading(false);
      });
  }, []);

  if (loading) return <p className="text-center">Loading...</p>;

  return (
    <div className="p-4">
      <h2 className="text-xl font-semibold mb-4">UK Price Data</h2>
      <table className="table-auto w-full border-collapse border border-gray-300">
        <thead className="bg-gray-100">
          <tr>
            <th className="border p-2">ID</th>
            <th className="border p-2">Product</th>
            <th className="border p-2">Price</th>
            <th className="border p-2">Update Time</th>
          </tr>
        </thead>
        <tbody>
          {prices.map((row) => (
            <tr key={row.id}>
              <td className="border p-2">{row.id}</td>
              <td className="border p-2">{row.product}</td>
              <td className="border p-2">£{row.price.toFixed(2)}</td>
              <td className="border p-2">{new Date(row.updateTime).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default UkPriceTable;
