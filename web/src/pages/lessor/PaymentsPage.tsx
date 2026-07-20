import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { lessorApi } from '../../api/lessorApi';
import { ErrorBanner, extractErrorMessage } from '../../components/ErrorBanner';
import { EmptyState } from '../../components/EmptyState';
import type { ReceiptResponse } from '../../api/types';

export function PaymentsPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [receipt, setReceipt] = useState<ReceiptResponse | null>(null);

  const { data: beds } = useQuery({ queryKey: ['bed-board'], queryFn: lessorApi.getBedBoard });
  const { data: invoices } = useQuery({ queryKey: ['invoices'], queryFn: () => lessorApi.listInvoices() });
  const { data: payments } = useQuery({ queryKey: ['payments'], queryFn: () => lessorApi.listPayments() });

  const [invoiceForm, setInvoiceForm] = useState({ bedId: '', totalAmount: '', dueDate: new Date().toISOString().slice(0, 10) });
  const generateInvoice = useMutation({
    mutationFn: () => lessorApi.generateInvoice({ bedId: Number(invoiceForm.bedId), totalAmount: Number(invoiceForm.totalAmount), dueDate: invoiceForm.dueDate }),
    onSuccess: () => {
      setInvoiceForm({ ...invoiceForm, totalAmount: '' });
      queryClient.invalidateQueries({ queryKey: ['invoices'] });
    },
    onError: (e) => setError(extractErrorMessage(e)),
  });

  const [paymentForm, setPaymentForm] = useState<Record<number, { amount: string; method: string; transactionId: string }>>({});
  const recordPayment = useMutation({
    mutationFn: (invoiceId: number) => {
      const f = paymentForm[invoiceId];
      return lessorApi.recordPayment({ invoiceId, amount: Number(f.amount), method: f.method, transactionId: f.transactionId || undefined });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['invoices'] });
      queryClient.invalidateQueries({ queryKey: ['payments'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
    onError: (e) => setError(extractErrorMessage(e)),
  });

  const viewReceipt = async (paymentId: number) => setReceipt(await lessorApi.getReceipt(paymentId));

  return (
    <div className="mx-auto max-w-5xl space-y-8 px-6 py-8">
      <h1 className="text-2xl font-bold text-brand">Payments &amp; invoices</h1>
      <ErrorBanner message={error} />

      <section className="rounded-lg border border-border bg-white p-5">
        <h2 className="mb-3 text-lg font-semibold">Generate a rent invoice</h2>
        <div className="flex flex-wrap gap-3">
          <select className="rounded-md border border-border px-3 py-1.5 text-sm" value={invoiceForm.bedId} onChange={(e) => setInvoiceForm({ ...invoiceForm, bedId: e.target.value })}>
            <option value="">Select bed</option>
            {beds?.map((b) => (
              <option key={b.id} value={b.id}>
                Room {b.roomNumber} - Bed {b.bedNumber}
              </option>
            ))}
          </select>
          <input
            type="number"
            className="w-32 rounded-md border border-border px-3 py-1.5 text-sm"
            placeholder="Amount"
            value={invoiceForm.totalAmount}
            onChange={(e) => setInvoiceForm({ ...invoiceForm, totalAmount: e.target.value })}
          />
          <input
            type="date"
            className="rounded-md border border-border px-3 py-1.5 text-sm"
            value={invoiceForm.dueDate}
            onChange={(e) => setInvoiceForm({ ...invoiceForm, dueDate: e.target.value })}
          />
          <button
            disabled={!invoiceForm.bedId || !invoiceForm.totalAmount || generateInvoice.isPending}
            onClick={() => generateInvoice.mutate()}
            className="rounded-md bg-brand px-4 py-1.5 text-sm font-medium text-white disabled:opacity-50"
          >
            Generate
          </button>
        </div>
      </section>

      <section>
        <h2 className="mb-3 text-lg font-semibold">Invoices</h2>
        {invoices?.length === 0 && <EmptyState message="No invoices yet." />}
        <div className="space-y-3">
          {invoices?.map((inv) => {
            const f = paymentForm[inv.id] ?? { amount: '', method: 'UPI', transactionId: '' };
            return (
              <div key={inv.id} className="rounded-lg border border-border bg-white p-4">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <span className="font-medium">
                    Bed {inv.bedNumber} &middot; Rs.{inv.totalAmount} total &middot; Rs.{inv.dueAmount} due
                  </span>
                  <span className="text-xs font-semibold text-brand">{inv.status.replace('_', ' ')}</span>
                </div>
                {inv.status !== 'PAID' && (
                  <div className="mt-3 flex flex-wrap gap-2">
                    <input
                      type="number"
                      className="w-24 rounded-md border border-border px-2 py-1 text-xs"
                      placeholder="Amount"
                      value={f.amount}
                      onChange={(e) => setPaymentForm({ ...paymentForm, [inv.id]: { ...f, amount: e.target.value } })}
                    />
                    <select
                      className="rounded-md border border-border px-2 py-1 text-xs"
                      value={f.method}
                      onChange={(e) => setPaymentForm({ ...paymentForm, [inv.id]: { ...f, method: e.target.value } })}
                    >
                      <option value="UPI">UPI</option>
                      <option value="CASH">Cash</option>
                      <option value="BANK_TRANSFER">Bank transfer</option>
                      <option value="OTHER">Other</option>
                    </select>
                    <input
                      className="w-32 rounded-md border border-border px-2 py-1 text-xs"
                      placeholder="Reference (optional)"
                      value={f.transactionId}
                      onChange={(e) => setPaymentForm({ ...paymentForm, [inv.id]: { ...f, transactionId: e.target.value } })}
                    />
                    <button
                      disabled={!f.amount || recordPayment.isPending}
                      onClick={() => recordPayment.mutate(inv.id)}
                      className="rounded-md bg-available px-3 py-1 text-xs font-medium text-white disabled:opacity-50"
                    >
                      Record payment
                    </button>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </section>

      <section>
        <h2 className="mb-3 text-lg font-semibold">Payment history</h2>
        {payments?.length === 0 && <EmptyState message="No payments recorded yet." />}
        <div className="space-y-2">
          {payments?.map((p) => (
            <div key={p.id} className="flex items-center justify-between rounded-lg border border-border bg-white p-3 text-sm">
              <span>
                Rs.{p.amount} via {p.method} &middot; {new Date(p.paymentDate).toLocaleString()}
              </span>
              <button onClick={() => viewReceipt(p.id)} className="text-xs font-medium text-brand">
                View receipt
              </button>
            </div>
          ))}
        </div>
      </section>

      {receipt && (
        <div className="fixed inset-0 flex items-center justify-center bg-black/40 p-4" onClick={() => setReceipt(null)}>
          <div className="w-full max-w-sm rounded-lg bg-white p-6" onClick={(e) => e.stopPropagation()}>
            <h3 className="text-lg font-bold text-brand">Receipt</h3>
            <dl className="mt-4 space-y-1 text-sm">
              <Row label="Hostel" value={receipt.hostelName} />
              <Row label="Room / Bed" value={`${receipt.roomNumber} / ${receipt.bedNumber}`} />
              <Row label="Renter" value={receipt.renterName} />
              <Row label="Amount" value={`Rs.${receipt.amount}`} />
              <Row label="Method" value={receipt.method} />
              {receipt.transactionId && <Row label="Reference" value={receipt.transactionId} />}
              <Row label="Date" value={new Date(receipt.paymentDate).toLocaleString()} />
              <Row label="Remaining due" value={`Rs.${receipt.remainingDue}`} />
            </dl>
            <button onClick={() => window.print()} className="mt-5 w-full rounded-md bg-brand px-4 py-2 text-sm font-semibold text-white">
              Print
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between">
      <dt className="text-ink-muted">{label}</dt>
      <dd className="font-medium">{value}</dd>
    </div>
  );
}
