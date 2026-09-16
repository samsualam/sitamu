import './style.css'
import { createClient } from '@supabase/supabase-js'

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL
const supabaseKey = import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY

const form = document.querySelector('#guest-form')
const submitButton = document.querySelector('#submit-button')
const destinationSelect = document.querySelector('#destination')
const destinationStatus = document.querySelector('#destination-status')
const configError = document.querySelector('#configuration-error')
const errorSummary = document.querySelector('#form-error-summary')
const successMessage = document.querySelector('#success-message')
const fields = ['name', 'phone', 'destination', 'purpose']

let supabase = null
let isSubmitting = false
let destinationsLoaded = false

function trimInputs() {
  for (const element of form.querySelectorAll('input, textarea')) {
    element.value = element.value.trim()
  }
}

function setFieldError(fieldName, message = '') {
  const field = document.querySelector(`#${fieldName}`)
  const error = document.querySelector(`#${fieldName}-error`)
  field.setAttribute('aria-invalid', message ? 'true' : 'false')
  error.textContent = message
}

function clearErrors() {
  errorSummary.hidden = true
  errorSummary.textContent = ''
  fields.forEach((field) => setFieldError(field))
}

function validateForm() {
  trimInputs()
  clearErrors()

  const values = Object.fromEntries(new FormData(form))
  const errors = {}

  if (!values.name) errors.name = 'Masukkan nama lengkap Anda.'
  if (!values.phone) errors.phone = 'Masukkan nomor WhatsApp Anda.'
  if (!values.destination) errors.destination = 'Pilih tujuan yang ingin ditemui.'
  if (!values.purpose) errors.purpose = 'Jelaskan keperluan kunjungan Anda.'

  for (const [field, message] of Object.entries(errors)) setFieldError(field, message)

  if (Object.keys(errors).length > 0) {
    errorSummary.textContent = 'Periksa kembali kolom yang wajib diisi.'
    errorSummary.hidden = false
    errorSummary.focus()
    return null
  }

  return values
}

function setSubmitState(submitting) {
  isSubmitting = submitting
  submitButton.disabled = submitting || !destinationsLoaded
  submitButton.querySelector('.button-label').hidden = submitting
  submitButton.querySelector('.button-loading').hidden = !submitting
}

function showDestinationLoadError() {
  destinationSelect.innerHTML = '<option value="">Tujuan tidak dapat dimuat</option>'
  destinationSelect.disabled = true
  destinationStatus.innerHTML = 'Daftar tujuan gagal dimuat. <button class="retry-button" type="button">Coba lagi</button>'
  destinationStatus.querySelector('.retry-button').addEventListener('click', loadDestinations)
}

async function loadDestinations() {
  destinationsLoaded = false
  setSubmitState(false)
  destinationSelect.disabled = true
  destinationSelect.innerHTML = '<option value="">Memuat daftar tujuan…</option>'
  destinationStatus.textContent = 'Sedang memuat tujuan yang tersedia.'

  try {
    const { data, error } = await supabase
      .from('destinations')
      .select('id, name, division')
      .eq('active', true)
      .order('name', { ascending: true })

    if (error) throw error

    if (!data?.length) {
      destinationSelect.innerHTML = '<option value="">Belum ada tujuan yang tersedia</option>'
      destinationStatus.textContent = 'Saat ini belum ada tujuan yang dapat dipilih.'
      return
    }

    destinationSelect.innerHTML = '<option value="">Pilih tujuan</option>'
    data.forEach((destination) => {
      const option = document.createElement('option')
      option.value = String(destination.id)
      option.dataset.name = destination.name
      option.textContent = `${destination.name} — ${destination.division}`
      destinationSelect.append(option)
    })
    destinationStatus.textContent = 'Pilih nama pegawai atau tujuan kunjungan.'
    destinationsLoaded = true
    destinationSelect.disabled = false
    setSubmitState(false)
  } catch (error) {
    console.error('Failed to load destinations:', error)
    showDestinationLoadError()
  }
}

async function submitVisit(event) {
  event.preventDefault()
  if (isSubmitting || !destinationsLoaded) return

  const values = validateForm()
  if (!values) return

  const selectedOption = destinationSelect.options[destinationSelect.selectedIndex]
  setSubmitState(true)

  try {
    const { error } = await supabase.from('guest_visits').insert({
      name: values.name,
      institution: values.institution || null,
      phone: values.phone,
      destination_id: Number(values.destination),
      destination_name: selectedOption.dataset.name,
      purpose: values.purpose,
    })

    if (error) throw error

    form.hidden = true
    successMessage.hidden = false
    successMessage.focus()
  } catch (error) {
    console.error('Failed to submit visit:', error)
    errorSummary.textContent = 'Permintaan belum dapat dikirim. Periksa koneksi Anda, lalu coba lagi.'
    errorSummary.hidden = false
    errorSummary.focus()
    setSubmitState(false)
  }
}

function initialize() {
  if (!supabaseUrl || !supabaseKey) {
    configError.textContent = 'Konfigurasi aplikasi belum tersedia. Hubungi petugas untuk bantuan.'
    configError.hidden = false
    destinationStatus.textContent = 'Daftar tujuan belum dapat dimuat.'
    return
  }

  supabase = createClient(supabaseUrl, supabaseKey)
  loadDestinations()
}

form.addEventListener('submit', submitVisit)
form.addEventListener('blur', (event) => {
  if (!fields.includes(event.target.id) || !event.target.value.trim()) return
  setFieldError(event.target.id)
}, true)

initialize()
