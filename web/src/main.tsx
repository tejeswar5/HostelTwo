import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { App } from './App';
import { RenterAuthProvider } from './auth/renterAuth';
import { LessorAuthProvider } from './auth/lessorAuth';
import './index.css';

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, staleTime: 10_000 } },
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <RenterAuthProvider>
        <LessorAuthProvider>
          <BrowserRouter>
            <App />
          </BrowserRouter>
        </LessorAuthProvider>
      </RenterAuthProvider>
    </QueryClientProvider>
  </StrictMode>,
);
