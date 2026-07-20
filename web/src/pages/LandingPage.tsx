import { Link } from 'react-router-dom';

export function LandingPage() {
  return (
    <main className="mx-auto flex min-h-screen max-w-4xl flex-col items-center justify-center gap-10 px-6 text-center">
      <div>
        <p className="font-mono text-xs tracking-[0.12em] text-ink-muted">HOSTEL ONE</p>
        <h1 className="mt-2 text-4xl font-extrabold tracking-tight text-brand sm:text-5xl">Find your place. Run it better.</h1>
        <p className="mx-auto mt-4 max-w-xl text-ink-muted">
          One platform for renters discovering a hostel bed and lessors running their property - availability, bookings, complaints
          and rent, all in one place.
        </p>
      </div>
      <div className="flex flex-col gap-4 sm:flex-row">
        <Link to="/renter" className="rounded-lg bg-brand px-8 py-4 text-lg font-semibold text-white hover:bg-brand-light">
          I&rsquo;m a renter
        </Link>
        <Link
          to="/lessor"
          className="rounded-lg border border-brand px-8 py-4 text-lg font-semibold text-brand hover:bg-surface-muted"
        >
          I&rsquo;m a lessor
        </Link>
      </div>
    </main>
  );
}
