export function ErrorBanner({ message }: { message: string | null }) {
  if (!message) return null;
  return <div className="mb-4 rounded-md bg-amber-100 px-4 py-3 text-sm text-amber-900">{message}</div>;
}

export function extractErrorMessage(error: unknown): string {
  const withResponse = error as { response?: { data?: { message?: string } } };
  return withResponse.response?.data?.message ?? 'Something went wrong. Please try again.';
}
