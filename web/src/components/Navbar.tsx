import { NavLink } from 'react-router-dom';

export function Navbar({
  brandLabel,
  links,
  userName,
  onLogout,
}: {
  brandLabel: string;
  links: { to: string; label: string }[];
  userName: string;
  onLogout: () => void;
}) {
  return (
    <header className="border-b border-border bg-white">
      <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-4 px-6 py-4">
        <div className="flex items-center gap-8">
          <span className="font-mono text-xs tracking-[0.12em] text-ink-muted">HOSTEL ONE</span>
          <nav className="flex flex-wrap gap-1">
            {links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                className={({ isActive }) =>
                  `rounded-md px-3 py-1.5 text-sm font-medium ${isActive ? 'bg-brand text-white' : 'text-ink-muted hover:bg-surface-muted'}`
                }
              >
                {link.label}
              </NavLink>
            ))}
          </nav>
        </div>
        <div className="flex items-center gap-3 text-sm">
          <span className="text-ink-muted">
            {brandLabel} &middot; {userName}
          </span>
          <button onClick={onLogout} className="rounded-md bg-surface-muted px-3 py-1.5 font-medium text-brand">
            Log out
          </button>
        </div>
      </div>
    </header>
  );
}
