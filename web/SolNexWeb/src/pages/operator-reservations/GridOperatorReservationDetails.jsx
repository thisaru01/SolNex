import {
  useCallback,
  useEffect,
  useState,
} from "react"

import {
  ArrowLeft,
} from "lucide-react"

import {
  useNavigate,
  useParams,
} from "react-router-dom"

import GridOperatorReservationDecision from "../../components/operator-reservations/GridOperatorReservationDecision"

import { gridOperatorReservationApi } from "../../services/gridOperatorReservationApi"

function formatDate(value) {
  if (!value) {
    return "—"
  }

  const date =
    new Date(value)

  return Number.isNaN(
    date.getTime(),
  )
    ? value
    : date.toLocaleString()
}

export default function GridOperatorReservationDetails() {
  const { id } =
    useParams()

  const navigate =
    useNavigate()

  const [
    reservation,
    setReservation,
  ] = useState(null)

  const [
    loading,
    setLoading,
  ] = useState(true)

  const [
    error,
    setError,
  ] = useState("")

  const loadReservation =
    useCallback(
      async () => {
        try {
          setLoading(true)
          setError("")

          const data =
            await gridOperatorReservationApi.getById(
              id,
            )

          setReservation(
            data,
          )
        } catch (err) {
          setError(
            err.message ||
              "Failed to load reservation details.",
          )
        } finally {
          setLoading(
            false,
          )
        }
      },
      [id],
    )

  useEffect(() => {
    loadReservation()
  }, [loadReservation])

  if (loading) {
    return (
      <p className="py-10 text-center text-muted-foreground">
        Loading reservation...
      </p>
    )
  }

  if (error) {
    return (
      <p className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
        {error}
      </p>
    )
  }

  if (!reservation) {
    return null
  }

  const fields = [
    [
      "Reservation ID",
      reservation.reservationId,
    ],
    [
      "Prosumer NIC",
      reservation.nic,
    ],
    [
      "Station ID",
      reservation.stationId,
    ],
    [
      "Slot ID",
      reservation.slotId,
    ],
    [
      "Day",
      reservation.dayOfWeek ||
        "—",
    ],
    [
      "Slot Time",
      reservation.startTime &&
      reservation.endTime
        ? `${reservation.startTime} - ${reservation.endTime}`
        : "—",
    ],
    [
      "Reservation Date",
      formatDate(
        reservation.reservationDate,
      ),
    ],
    [
      "Energy Amount",
      `${reservation.energyAmountKwh} kWh`,
    ],
    [
      "Status",
      reservation.status,
    ],
    [
      "Created At",
      formatDate(
        reservation.createdAt,
      ),
    ],
  ]

  async function handleDecisionCompleted(
    result,
  ) {
    if (
      result?.reservation
    ) {
      setReservation(
        (current) => ({
          ...current,
          ...result.reservation,
        }),
      )
    } else {
      await loadReservation()
    }
  }

  return (
    <div className="space-y-6">
      <button
        type="button"
        onClick={() =>
          navigate(
            "/operator/reservations",
          )
        }
        className="inline-flex items-center gap-2 text-sm font-medium text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" />

        Back to Grid Operator
        Reservations
      </button>

      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          Grid Operator
          Reservation Details
        </h1>

        <p className="text-muted-foreground">
          Review the reservation
          information before approving
          or rejecting it.
        </p>
      </div>

      <div className="grid gap-4 rounded-xl border bg-background p-6 shadow-sm md:grid-cols-2">
        {fields.map(
          ([label, value]) => (
            <div
              key={label}
              className="rounded-lg border bg-muted/20 p-4"
            >
              <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                {label}
              </p>

              <p className="mt-1 break-words font-semibold">
                {value}
              </p>
            </div>
          ),
        )}
      </div>

      {reservation.status ===
      "Pending" ? (
        <GridOperatorReservationDecision
          reservationId={
            reservation.reservationId
          }
          onCompleted={
            handleDecisionCompleted
          }
        />
      ) : (
        <div className="rounded-xl border bg-muted/20 p-5 text-sm">
          This reservation has
          already been{" "}
          <strong>
            {
              reservation.status
            }
          </strong>
          .

          {reservation.rejectedReason
            ? ` Reason: ${reservation.rejectedReason}`
            : ""}
        </div>
      )}
    </div>
  )
}