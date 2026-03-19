import { TimelineEvent } from "@/data/mockData";

interface TimelineProps {
  events: TimelineEvent[];
}

const typeStyles: Record<TimelineEvent["type"], { dot: string; label: string }> = {
  meeting: { dot: "bg-primary", label: "Reunião" },
  review: { dot: "bg-status-growing", label: "Revisão" },
  note: { dot: "bg-status-stable", label: "Nota" },
  alert: { dot: "bg-status-at-risk", label: "Alerta" },
};

const Timeline = ({ events }: TimelineProps) => {
  return (
    <div className="relative">
      <div className="absolute left-3 top-0 bottom-0 w-px bg-border" />
      <div className="space-y-8">
        {events.map((event) => {
          const style = typeStyles[event.type];
          return (
            <div key={event.id} className="relative pl-10">
              <div className={`absolute left-1.5 top-1.5 w-3 h-3 rounded-full ${style.dot}`} />
              <div>
                <div className="flex items-center gap-3 mb-1">
                  <span className="text-xs font-heading font-medium text-muted-foreground uppercase tracking-wider">{style.label}</span>
                  <span className="text-xs text-muted-foreground">{event.date}</span>
                </div>
                <h4 className="font-heading font-medium text-foreground">{event.title}</h4>
                <p className="text-sm text-muted-foreground mt-1">{event.description}</p>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default Timeline;
