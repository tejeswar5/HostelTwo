import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout, FormField, inputClass, primaryButtonClass } from './AuthLayout';
import { registerSchema, type RegisterFormValues } from './schemas';
import { lessorApi } from '../../api/lessorApi';
import { useLessorAuth } from '../../auth/lessorAuth';
import { ErrorBanner, extractErrorMessage } from '../../components/ErrorBanner';

export function LessorRegisterPage() {
  const { login } = useLessorAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormValues>({ resolver: zodResolver(registerSchema) });

  const onSubmit = async (values: RegisterFormValues) => {
    setError(null);
    try {
      const auth = await lessorApi.register(values);
      login(auth);
      navigate('/lessor/dashboard');
    } catch (e) {
      setError(extractErrorMessage(e));
    }
  };

  return (
    <AuthLayout
      title="Create your lessor account"
      subtitle="Set up your hostel and start managing it in minutes."
      footer={
        <>
          Already have an account?{' '}
          <Link to="/lessor/login" className="font-medium text-brand">
            Sign in
          </Link>
        </>
      }
    >
      <ErrorBanner message={error} />
      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
        <div className="grid grid-cols-2 gap-3">
          <FormField label="First name" error={errors.fname?.message}>
            <input className={inputClass} {...register('fname')} />
          </FormField>
          <FormField label="Last name" error={errors.lname?.message}>
            <input className={inputClass} {...register('lname')} />
          </FormField>
        </div>
        <FormField label="Email" error={errors.email?.message}>
          <input className={inputClass} type="email" {...register('email')} />
        </FormField>
        <FormField label="Phone number" error={errors.phoneNumber?.message}>
          <input className={inputClass} {...register('phoneNumber')} />
        </FormField>
        <FormField label="Password" error={errors.password?.message}>
          <input className={inputClass} type="password" {...register('password')} />
        </FormField>
        <button className={primaryButtonClass} disabled={isSubmitting} type="submit">
          Create account
        </button>
      </form>
    </AuthLayout>
  );
}
