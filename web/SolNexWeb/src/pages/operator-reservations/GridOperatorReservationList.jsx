import {
  useEffect,
  useMemo,
  useState,
} from "react"

import { useNavigate } from "react-router-dom"

import {
  RefreshCw,
  Search,
} from "lucide-react"

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

export default function GridOperatorReservationList() {
  const navigate =
    useNavigate()

  const [
    reservations,
    setReservations,
  ] = useState([])

  const [
    search,
    setSearch,
  ] = useState("")

  const [
    loading,
    setLoading,
  ] = useState(true)

  const [
    error,
    setError,
  ] = useState("")

  async function loadReservations() {
    try {
      setLoading(true)
      setError("")

      const data =
        await gridOperatorReservationApi.getPending()

      setReservations(
        Array.isArray(data)
          ? data
          : [],
      )
    } catch (err) {
      setError(
        err.message ||
          "Failed to load pending reservations.",
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadReservations()
  }, [])

  const filteredReservations =
    useMemo(() => {
      const query =
        search
          .trim()
          .toLowerCase()

      if (!query) {
        return reservations
      }

      return reservations.filter(
        (reservation) =>
          [
            reservation.reservationId,
            reservation.nic,
            reservation.stationId,
            reservation.slotId,
            reservation.status,
          ].some((value) =>
            String(
              value || "",
            )
              .toLowerCase()
              .includes(query),
          ),
      )
    }, [
      reservations,
      search,
    ])

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            Grid Operator
            Reservations
          </h1>

          <p className="text-muted-foreground">
            Review pending prosumer
            reservations and make an
            approval decision.
          </p>
        </div>

        <button
          type="button"
          onClick={
            loadReservations
          }
          className="inline-flex h-10 items-center justify-center gap-2 rounded-md border bg-background px-4 text-sm font-medium hover:bg-muted"
        >
          <RefreshCw className="h-4 w-4" />

          Refresh
        </button>
      </div>

      <div className="rounded-xl border bg-background shadow-sm">
        <div className="border-b p-4">
          <div className="relative max-w-md">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />

            <input
              value={search}
              onChange={(
                event,
              ) =>
                setSearch(
                  event.target
                    .value,
                )
              }
              placeholder="Search reservation, NIC, station or slot..."
              className="h-10 w-full rounded-md border bg-background pl-9 pr-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
            />
          </div>
        </div>

        {error && (
          <p className="m-4 rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
            {error}
          </p>
        )}

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="border-b bg-muted/50 text-left">
              <tr>
                <th className="px-4 py-3 font-medium">
                  Reservation ID
                </th>

                <th className="px-4 py-3 font-medium">
                  Prosumer NIC
                </th>

                <th className="px-4 py-3 font-medium">
                  Station
                </th>

                <th className="px-4 py-3 font-medium">
                  Slot
                </th>

                <th className="px-4 py-3 font-medium">
                  Reservation Date
                </th>

                <th className="px-4 py-3 font-medium">
                  Energy
                </th>

                <th className="px-4 py-3 font-medium">
                  Status
                </th>

                <th className="px-4 py-3 text-right font-medium">
                  Action
                </th>
              </tr>
            </thead>

            <tbody>
              {loading ? (
                <tr>
                  <td
                    colSpan="8"
                    className="px-4 py-10 text-center text-muted-foreground"
                  >
                    Loading pending
                    reservations...
                  </td>
                </tr>
              ) : filteredReservations.length ===
                0 ? (
                <tr>
                  <td
                    colSpan="8"
                    className="px-4 py-10 text-center text-muted-foreground"
                  >
                    No pending
                    reservations
                    found.
                  </td>
                </tr>
              ) : (
                filteredReservations.map(
                  (
                    reservation,
                  ) => (
                    <tr
                      key={
                        reservation.id ||
                        reservation.reservationId
                      }
                      className="border-b last:border-0 hover:bg-muted/30"
                    >
                      <td className="px-4 py-3 font-medium">
                        {
                          reservation.reservationId
                        }
                      </td>

                      <td className="px-4 py-3">
                        {
                          reservation.nic
                        }
                      </td>

                      <td className="px-4 py-3">
                        {
                          reservation.stationId
                        }
                      </td>

                      <td className="px-4 py-3">
                        {
                          reservation.slotId
                        }
                      </td>

                      <td className="px-4 py-3">
                        {formatDate(
                          reservation.reservationDate,
                        )}
                      </td>

                      <td className="px-4 py-3">
                        {
                          reservation.energyAmountKwh
                        }{" "}
                        kWh
                      </td>

                      <td className="px-4 py-3">
                        <span className="rounded-full bg-amber-100 px-2.5 py-1 text-xs font-semibold text-amber-700">
                          {
                            reservation.status
                          }
                        </span>
                      </td>

                      <td className="px-4 py-3 text-right">
                        <button
                          type="button"
                          onClick={() =>
                            navigate(
                              `/operator/reservations/${encodeURIComponent(
                                reservation.reservationId,
                              )}`,
                            )
                          }
                          className="rounded-md border px-3 py-1.5 text-xs font-medium hover:bg-muted"
                        >
                          Review
                        </button>
                      </td>
                    </tr>
                  ),
                )
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}