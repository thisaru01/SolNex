import { useState } from "react"

import {
  reservationApprovalApi,
} from "../../services/reservationApprovalApi"

export default function BackofficeReservationActions({
  reservation,
  onApproved,
  onRejected,
  onCancel,
}) {
  const [
    loadingAction,
    setLoadingAction,
  ] = useState(null)

  const [
    error,
    setError,
  ] = useState(null)

  const [
    showRejectForm,
    setShowRejectForm,
  ] = useState(false)

  const [
    reason,
    setReason,
  ] = useState("")

  const handleApprove = async () => {
    try {
      setLoadingAction(
        "approve"
      )

      setError(
        null
      )

      const result =
        await reservationApprovalApi
          .approve(
            reservation
              .reservationId
          )

      /*
       * Inform ReservationList that
       * approval completed successfully.
       */
      if (onApproved) {
        onApproved(
          result
        )
      }

    } catch (err) {
      const message =
        err.message ||
        "Failed to approve reservation."

      setError(
        message
      )

    } finally {
      setLoadingAction(
        null
      )
    }
  }

  const handleReject = async () => {
    const trimmedReason =
      reason.trim()

    if (
      trimmedReason.length <
      3
    ) {
      setError(
        "Please enter a valid rejection reason."
      )

      return
    }

    try {
      setLoadingAction(
        "reject"
      )

      setError(
        null
      )

      const result =
        await reservationApprovalApi
          .reject(
            reservation
              .reservationId,
            trimmedReason
          )

      /*
       * Inform ReservationList that
       * rejection completed successfully.
       */
      if (onRejected) {
        onRejected(
          result
        )
      }

      setShowRejectForm(false)
      setReason("")

    } catch (err) {
      const message =
        err.message ||
        "Failed to reject reservation."

      setError(
        message
      )

    } finally {
      setLoadingAction(
        null
      )
    }
  }

  /*
   * Actions are available only while
   * the reservation is Pending.
   */
  if (
    !reservation ||
    reservation.status !==
      "Pending"
  ) {
    return null
  }

  return (
    <div className="space-y-3">

      {error && (
        <div className="rounded-md bg-red-50 p-3 text-sm text-red-700 border border-red-200">
          {error}
        </div>
      )}

      {!showRejectForm ? (
        <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <button
            type="button"
            onClick={onCancel}
            disabled={loadingAction !== null}
            className="inline-flex h-11 items-center justify-center rounded-md border border-border px-5 text-sm font-semibold text-foreground transition-colors hover:bg-muted disabled:cursor-not-allowed disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={() => {
              setShowRejectForm(true)
              setError(null)
            }}
            disabled={loadingAction !== null}
            className="inline-flex h-11 items-center justify-center rounded-md border border-red-200 px-5 text-sm font-semibold text-red-700 transition-colors hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-red-900 dark:text-red-300 dark:hover:bg-red-950/40"
          >
            Reject
          </button>
          <button
            type="button"
            onClick={handleApprove}
            disabled={loadingAction !== null}
            className="inline-flex h-11 items-center justify-center rounded-md bg-emerald-600 px-5 text-sm font-semibold text-white transition-colors hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {loadingAction === "approve"
              ? "Approving..."
              : "Approve"}
          </button>
        </div>
      ) : (
        <div className="space-y-3 rounded-lg border border-border bg-muted/30 p-4">
          <label
            htmlFor="backoffice-rejection-reason"
            className="block text-sm font-medium"
          >
            Rejection reason
          </label>
          <textarea
            id="backoffice-rejection-reason"
            rows={3}
            value={reason}
            onChange={(event) => {
              setReason(event.target.value)
              setError(null)
            }}
            className="w-full resize-y rounded-md border border-border bg-background px-3 py-2 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
          />
          <div className="flex justify-end gap-2">
            <button
              type="button"
              onClick={() => {
                setShowRejectForm(false)
                setReason("")
                setError(null)
              }}
              disabled={loadingAction !== null}
              className="rounded-md border border-border px-4 py-2 text-sm font-medium hover:bg-muted disabled:opacity-50"
            >
              Back
            </button>
            <button
              type="button"
              onClick={handleReject}
              disabled={
                loadingAction !== null ||
                reason.trim().length < 3
              }
              className="rounded-md bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {loadingAction === "reject"
                ? "Rejecting..."
                : "Confirm Rejection"}
            </button>
          </div>
        </div>
      )}

    </div>
  )
}