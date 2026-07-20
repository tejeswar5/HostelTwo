import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout, FormField, inputClass, primaryButtonClass } from './AuthLayout';
import { loginSchema, type LoginFormValues } from './schemas';
import { lessorApi } from '../../api/lessorApi';
import { useLessorAuth } from '../../auth/lessorAuth';
import { ErrorBanner, extractErrorMessage } from '../../components/ErrorBanner';

export function LessorLoginPage() {
  const { login } = useLessorAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({ resolver: zodResolver(loginSchema) });

  const onSubmit = async (values: LoginFormValues) => {
    setError(null);
    try {
      const auth = await lessorApi.login(values);
      login(auth);
      navigate('/lessor/dashboard');
    } catch (e) {
      setError(extractErrorMessage(e));
    }
  };

  return (
    <AuthLayout
      title="Lessor sign in"
      subtitle="Manage your hostel, bookings, maintenance and rent."
      footer={
        <>
          New here?{' '}
          <Link to="/lessor/register" className="font-medium text-brand">
            Create an account
          </Link>
        </>
      }
    >
      <ErrorBanner message={error} />
      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
        <FormField label="Email" error={errors.email?.message}>
          <input className={inputClass} type="email" {...register('email')} />
        </FormField>
        <FormField label="Password" error={errors.password?.message}>
          <input className={inputClass} type="password" {...register('password')} />
        </FormField>
        <button className={primaryButtonClass} disabled={isSubmitting} type="submit">
          Sign in
        </button>
      </form>
    </AuthLayout>
  );
}
