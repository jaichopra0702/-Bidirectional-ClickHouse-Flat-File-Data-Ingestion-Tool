import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';

// Test case to check if the ClickHouseIngestionForm is rendered
test('renders ClickHouseIngestionForm component', () => {
  render(<App />);

  // Check if the ClickHouseIngestionForm component is rendered
  const element = screen.getByText(/Ingestion started, checking status/i); // You can replace this with any text you expect to be in the ClickHouseIngestionForm
  expect(element).toBeInTheDocument();
});

// If you want to test that other components are not rendered, you can add checks for those:
test('does not render UkPriceTable component', () => {
  render(<App />);
  const ukPriceTableElement = screen.queryByText(/Price Table/i); // Replace with specific text or element you expect in UkPriceTable
  expect(ukPriceTableElement).toBeNull(); // Should be null since it's commented out
});

test('does not render IngestionForm component', () => {
  render(<App />);
  const ingestionFormElement = screen.queryByText(/Ingestion Form/i); // Replace with specific text or element you expect in IngestionForm
  expect(ingestionFormElement).toBeNull(); // Should be null since it's commented out
});
