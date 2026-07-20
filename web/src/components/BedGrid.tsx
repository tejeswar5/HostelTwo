import type { ReactNode } from 'react';
import type { BedStatus } from '../api/types';

const STATUS_STYLES: Record<BedStatus, string> = {
  AVAILABLE: 'border-available',
  BOOKED: 'border-booked',
  MAINTENANCE: 'border-maintenance',
};

const STATUS_LABEL: Record<BedStatus, string> = {
  AVAILABLE: 'Available',
  BOOKED: 'Booked',
  MAINTENANCE: 'Maintenance',
};

export interface BedGridItem {
  id: number;
  bedNumber: string;
  status: BedStatus;
}

export function BedGrid<T extends BedGridItem>({
  beds,
  renderMeta,
  renderActions,
}: {
  beds: T[];
  renderMeta: (bed: T) => ReactNode;
  renderActions?: (bed: T) => ReactNode;
}) {
  return (
    <div>
      <div className="mb-4 flex items-center gap-5 text-xs text-ink-muted">
        <Legend colorClass="bg-available" label="Available" />
        <Legend colorClass="bg-booked" label="Booked" />
        <Legend colorClass="bg-maintenance" label="Maintenance" />
      </div>
      <div className="grid grid-cols-[repeat(auto-fill,minmax(170px,1fr))] gap-3">
        {beds.map((bed) => (
          <article key={bed.id} className={`flex flex-col gap-2 rounded-md border-l-4 bg-surface-muted/40 p-4 ${STATUS_STYLES[bed.status]}`}>
            <div className="flex items-center justify-between">
              <strong className="text-lg">{bed.bedNumber}</strong>
              <span className="font-mono text-[11px] text-ink-muted">{STATUS_LABEL[bed.status]}</span>
            </div>
            <div className="text-xs text-ink-muted">{renderMeta(bed)}</div>
            {renderActions?.(bed)}
          </article>
        ))}
        {beds.length === 0 && <p className="text-sm text-ink-muted">No beds to show yet.</p>}
      </div>
    </div>
  );
}

function Legend({ colorClass, label }: { colorClass: string; label: string }) {
  return (
    <span className="flex items-center gap-1.5">
      <i className={`inline-block h-2.5 w-2.5 rounded-full ${colorClass}`} />
      {label}
    </span>
  );
}
