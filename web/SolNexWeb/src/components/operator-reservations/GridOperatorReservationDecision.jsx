import { useState } from "react"
import {
  CheckCircle2,
  XCircle,
} from "lucide-react"

import { gridOperatorReservationApi } from "../../services/gridOperatorReservationApi"

function getOperatorNic() {
  try {
    const user = JSON.parse(
      localStorage.getItem(
        "solnex_user",
      ) || "null",
    )

    return user?.nic || ""
  } catch {
    return ""
  }
}

export default function GridOperatorReservationDecision({
  reservationId,
  onCompleted,
}) {
  const [showReject, setShowReject] =
    useState(false)

  const [reason, setReason] =
    useState("")

  const [loading, setLoading] =
    useState("")

  const [message, setMessage] =
    useState("")

  const [error, setError] =
    useState("")

  async function handleApprove() {
    const operatorNic =
      getOperatorNic()

    if (!operatorNic) {
      setError(
        "Grid Operator NIC is unavailable. Please sign in again.",
      )
      return
    }

    const confirmed = window.confirm(
      "Approve this reservation and create its energy transaction?",
    )

    if (!confirmed) {
      return
    }

    try {
      setLoading("approve")
      setMessage("")
      setError("")

      const result =
        await gridOperatorReservationApi.approve(
          reservationId,
          operatorNic,
        )

      setMessage(
        result?.message ||
          "Reservation approved successfully.",
      )

      await onCompleted?.(result)
    } catch (err) {
      setError(
        err.message ||
          "Could not approve the reservation.",
      )
    } finally {
      setLoading("")
    }
  }

  async function handleReject() {
    const operatorNic =
      getOperatorNic()

    const trimmedReason =
      reason.trim()

    if (!operatorNic) {
      setError(
        "Grid Operator NIC is unavailable. Please sign in again.",
      )
      return
    }

    if (
      trimmedReason.length < 3
    ) {
      setError(
        "Enter a valid rejection reason.",
      )
      return
    }

    try {
      setLoading("reject")
      setMessage("")
      setError("")

      const result =
        await gridOperatorReservationApi.reject(
          reservationId,
          operatorNic,
          trimmedReason,
        )

      setMessage(
        result?.message ||
          "Reservation rejected successfully.",
      )

      setReason("")
      setShowReject(false)

      await onCompleted?.(result)
    } catch (err) {
      setError(
        err.message ||
          "Could not reject the reservation.",
      )
    } finally {
      setLoading("")
    }
  }

  return (
    <section className="rounded-xl border bg-background p-5 shadow-sm">
      <div className="mb-4">
        <h2 className="text-lg font-semibold">
          Grid Operator Decision
        </h2>

        <p className="text-sm text-muted-foreground">
          Approve this pending reservation
          or reject it with a reason.
        </p>
      </div>

      {message && (
        <p className="mb-4 rounded-md border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">
          {message}
        </p>
      )}

      {error && (
        <p className="mb-4 rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
          {error}
        </p>
      )}

      {!showReject ? (
        <div className="flex flex-col gap-3 sm:flex-row sm:justify-end">
          <button
            type="button"
            disabled={Boolean(
              loading,
            )}
            onClick={() => {
              setShowReject(true)
              setMessage("")
              setError("")
            }}
            className="inline-flex h-10 items-center justify-center gap-2 rounded-md border border-destructive px-4 text-sm font-medium text-destructive hover:bg-destructive/10 disabled:opacity-50"
          >
            <XCircle className="h-4 w-4" />

            Reject Reservation
          </button>

          <button
            type="button"
            disabled={Boolean(
              loading,
            )}
            onClick={
              handleApprove
            }
            className="inline-flex h-10 items-center justify-center gap-2 rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:opacity-50"
          >
            <CheckCircle2 className="h-4 w-4" />

            {loading ===
            "approve"
              ? "Approving..."
              : "Approve Reservation"}
          </button>
        </div>
      ) : (
        <div className="space-y-3 rounded-lg border border-destructive/20 bg-destructive/5 p-4">
          <label
            htmlFor="operatorRejectionReason"
            className="text-sm font-medium"
          >
            Rejection reason
          </label>

          <textarea
            id="operatorRejectionReason"
            rows={4}
            value={reason}
            onChange={(event) =>
              setReason(
                event.target.value,
              )
            }
            placeholder="Explain why this reservation cannot be approved..."
            className="w-full rounded-md border bg-background px-3 py-2 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
          />

          <div className="flex justify-end gap-2">
            <button
              type="button"
              disabled={
                loading ===
                "reject"
              }
              onClick={() => {
                setShowReject(false)
                setReason("")
                setError("")
              }}
              className="h-10 rounded-md border px-4 text-sm font-medium hover:bg-muted disabled:opacity-50"
            >
              Cancel
            </button>

            <button
              type="button"
              onClick={
                handleReject
              }
              disabled={
                loading ===
                  "reject" ||
                reason.trim()
                  .length < 3
              }
              className="h-10 rounded-md bg-destructive px-4 text-sm font-medium text-destructive-foreground hover:bg-destructive/90 disabled:opacity-50"
            >
              {loading ===
              "reject"
                ? "Rejecting..."
                : "Confirm Reject"}
            </button>
          </div>
        </div>
      )}
    </section>
  )
}